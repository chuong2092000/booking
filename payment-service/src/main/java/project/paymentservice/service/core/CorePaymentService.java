package project.paymentservice.service.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import project.commondto.dto.KafkaTopic;
import project.commondto.dto.OutBoxDto;
import project.commondto.dto.booking.BookingDto;
import project.commondto.dto.booking.BookingStatus;
import project.commondto.dto.payment.CreatePaymentDto;
import project.commondto.dto.payment.PaymentDto;
import project.commondto.dto.payment.PaymentMethod;
import project.commondto.dto.payment.PaymentStatus;
import project.commondto.dto.payment.outbox.BookingPaymentDto;
import project.commondto.exception.BusinessException;
import project.commonutils.BaseUtils;
import project.paymentservice.client.BookingClient;
import project.paymentservice.entity.Payment;
import project.paymentservice.mapper.PaymentMapper;
import project.paymentservice.repo.PaymentRepo;
import project.paymentservice.service.impl.OutboxService;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CorePaymentService {

    private final PaymentRepo paymentRepo;

    private final BookingClient bookingClient;

    private final OutboxService outboxService;

    private final ObjectMapper objectMapper;

    private final RedissonClient redissonClient;

    private final PaymentMapper paymentMapper;

    public List<Payment> createPayments(CreatePaymentDto createPaymentDto, String transactionCode, String ipAddress, Instant expireDate) {
        BaseUtils.validateObject(createPaymentDto, "Create payment DTO", false);
        BaseUtils.validateObject(createPaymentDto.getPaymentMethod(), "Payment method", false);
        BaseUtils.validateObject(createPaymentDto.getPaymentGateway(), "Payment gateway", false);

        List<String> bookingIds = createPaymentDto.getBookingIds().stream().distinct().toList();
        BaseUtils.validateListObject(bookingIds, "Booking Ids DTO", false);

        List<BookingDto> bookingsDto = bookingClient.getBookingsHaveBookingInventoriesLockedByBookingIds(bookingIds);
        BaseUtils.validateListObject(bookingsDto, "Bookings DTO", false);

        Map<String, BookingDto> bookingDtoMap = bookingsDto.stream()
                .collect(Collectors.toMap(BookingDto::getId, b -> b, (old, val) -> old));

        List<String> missingIds = bookingIds.stream()
                .filter(id -> !bookingDtoMap.containsKey(id))
                .toList();
        if (!missingIds.isEmpty()) {
            log.warn("Bookings not found with ids: {}", missingIds);
            throw new BusinessException("Booking not found with ids: " + missingIds);
        }

        List<Payment> payments = bookingIds.stream()
                .map(id -> toPayment(bookingDtoMap.get(id), createPaymentDto, transactionCode, ipAddress, expireDate))
                .toList();

        return paymentRepo.saveAll(payments);
    }

    public List<Payment> processRefundPayment(List<String> paymentIds) {
        BaseUtils.validateListObject(paymentIds, "Payment IDS", false);

        List<String> distinctPaymentIds = paymentIds.stream().distinct().toList();

        List<Payment> payments = paymentRepo.getPaymentByIdsAndIsDeleted(distinctPaymentIds, false);
        if (payments.isEmpty()) {
            log.warn("Payment not found with ids: {}", distinctPaymentIds);
            throw new BusinessException("Payment not found with ids: " + distinctPaymentIds);
        }

        if (payments.size() != distinctPaymentIds.size()) {
            List<String> foundIds = payments.stream().map(Payment::getId).toList();
            List<String> missingIds = distinctPaymentIds.stream()
                    .filter(id -> !foundIds.contains(id))
                    .toList();
            log.warn("Some payment ids not found: {}", missingIds);
            throw new BusinessException("Payment not found with ids: " + missingIds);
        }

        List<String> invalidStatusIds = payments.stream()
                .filter(p -> p.getPaymentStatus() != PaymentStatus.PAID)
                .map(Payment::getId)
                .toList();
        if (!invalidStatusIds.isEmpty()) {
            log.warn("Payments not in PAID status, cannot refund: {}", invalidStatusIds);
            throw new BusinessException("Payment invalid status, expected PAID for ids: " + invalidStatusIds);
        }

        payments.forEach(p -> {
            if (p.getPaymentMethod() == PaymentMethod.CASH) {
                p.setPaymentStatus(PaymentStatus.REFUNDED);
                p.setRefundedAmount(p.getAmount());
                p.setRefundedAt(BaseUtils.getInstantNow());
            } else {
                p.setPaymentStatus(PaymentStatus.PROCESSING_REFUND);
            }
        });

        return payments;
    }

    public void updatePaidCashPayment(String paymentId) {
        BaseUtils.validateObject(paymentId, "Payment id", false);
        Payment payment = paymentRepo.getPaymentByIdAndPaymentMethodAndIsDeleted(paymentId, PaymentMethod.CASH, false).orElseThrow(() -> {
            log.warn("Payment not found id: {}", paymentId);
            return new BusinessException("Payment not found id: " + paymentId);
        });

        payment.setPaymentStatus(PaymentStatus.PAID);
        payment.setPaidAt(BaseUtils.getInstantNow());

        OutBoxDto outBoxDto = OutBoxDto.builder()
                .payload(BaseUtils.convertObjectToString(objectMapper, paymentMapper.toDto(payment)))
                .topic(KafkaTopic.PAYMENT_CASH_UPDATE_PAID)
                .build();
        outboxService.createOutbox(outBoxDto);
    }

    public void updatePaymentRefundedSuccess(String id, Instant refundedAt, BigDecimal refundedAmount) {
        BaseUtils.validateObject(id, "Payment id", false);
        BaseUtils.validateObject(refundedAt, "Payment refunded at", false);
        BaseUtils.validateObject(refundedAmount, "Payment refunded amount", false);

        Payment payment = paymentRepo.getPaymentByIdAndIsDeleted(id, false)
                .orElseThrow(() -> {
                    log.warn("Payment not found id: {}", id);
                    return new BusinessException("Payment not found id: " + id);
                });

        payment.setRefundedAt(refundedAt);
        payment.setRefundedAmount(refundedAmount);
        payment.setPaymentStatus(PaymentStatus.REFUNDED);

        OutBoxDto outBoxDto = OutBoxDto.builder()
                .payload(BaseUtils.convertObjectToString(objectMapper, paymentMapper.toDto(payment)))
                .topic(KafkaTopic.PAYMENT_REFUNDED)
                .build();
        outboxService.createOutbox(outBoxDto);
    }

    public void updatePaymentRefundedFailed(String id, String failureReason) {
        BaseUtils.validateObject(id, "Payment id", false);

        Payment payment = paymentRepo.getPaymentByIdAndIsDeleted(id, false)
                .orElseThrow(() -> {
                    log.warn("Payment not found id: {}", id);
                    return new BusinessException("Payment not found id: " + id);
                });

        payment.setPaymentStatus(PaymentStatus.REFUND_FAILED);
        payment.setFailureReason(failureReason);

        OutBoxDto outBoxDto = OutBoxDto.builder()
                .payload(BaseUtils.convertObjectToString(objectMapper, paymentMapper.toDto(payment)))
                .topic(KafkaTopic.PAYMENT_REFUNDED_FAILED)
                .build();
        outboxService.createOutbox(outBoxDto);
    }

    public List<Payment> getPaymentsByTransactionCode(String transactionCode) {
        BaseUtils.validateObject(transactionCode, "Transaction code", false);
        return paymentRepo.getPaymentsByTransactionCodeAndIsDeleted(transactionCode, false);
    }

    public void createOutboxRefund(List<PaymentDto> paymentsDto) {
        BaseUtils.validateListObject(paymentsDto, "Payments DTO", false);

        List<OutBoxDto> outBoxesDto = new ArrayList<>();

        for (PaymentDto p : paymentsDto) {
            String payload = BaseUtils.convertObjectToString(objectMapper, p);
            boolean isCash = PaymentMethod.CASH.equals(p.getPaymentMethod());

            String paymentTopic = isCash
                    ? KafkaTopic.PAYMENT_REFUNDED
                    : KafkaTopic.PAYMENT_PROCESSING_REFUND;

            String notifyTopic = isCash
                    ? KafkaTopic.NOTIFICATION_PAYMENT_REFUNDED
                    : KafkaTopic.NOTIFICATION_PAYMENT_REFUNDING;

            outBoxesDto.add(OutBoxDto.builder().payload(payload).topic(paymentTopic).build());
            outBoxesDto.add(OutBoxDto.builder().payload(payload).topic(notifyTopic).build());
        }

        outboxService.createOutboxes(outBoxesDto);
    }

    public void createOutboxProcessingPaid(List<PaymentDto> paymentsDto) {
        List<BookingPaymentDto> bookingPaymentsDto = paymentsDto.stream().map(ps ->
                BookingPaymentDto.builder()
                        .bookingId(ps.getBookingId())
                        .paymentId(ps.getId())
                        .paymentGateway(ps.getPaymentGateway())
                        .paymentStatus(ps.getPaymentStatus())
                        .paymentMethod(ps.getPaymentMethod())
                        .createdDate(ps.getCreatedDate())
                        .expireDate(ps.getExpireDate())
                        .build()
        ).toList();
        OutBoxDto outBoxDto = OutBoxDto.builder()
                .payload(BaseUtils.convertObjectToString(objectMapper, bookingPaymentsDto))
                .topic(KafkaTopic.PAYMENT_PROCESSING_PAID)
                .build();
        outboxService.createOutbox(outBoxDto);
    }

    public void createOutboxPaidOrFailed(List<Payment> payments, String topic) {
        BaseUtils.validateListObject(payments, "Payments", false);

        List<BookingPaymentDto> bookingPaymentsDto = payments.stream()
                .map(payment -> BookingPaymentDto.builder()
                        .bookingId(payment.getBookingId())
                        .paymentId(payment.getId())
                        .paymentGateway(payment.getPaymentGateway())
                        .paymentStatus(payment.getPaymentStatus())
                        .paymentMethod(payment.getPaymentMethod())
                        .createdDate(payment.getCreatedDate())
                        .build())
                .toList();

        OutBoxDto outBoxBooking = OutBoxDto.builder()
                .payload(BaseUtils.convertObjectToString(objectMapper, bookingPaymentsDto))
                .topic(topic)
                .build();
        outboxService.createOutboxes(List.of(outBoxBooking));
    }

    public void publishOutboxNotifyPayment(List<Payment> payments, String topic) {
        BaseUtils.validateListObject(payments, "Payments", false);
        String notifyTopic = TARGET_NOTIFY.get(topic);
        if (notifyTopic == null) {
            throw new BusinessException("No notify topic mapped for topic: " + topic);
        }
        List<OutBoxDto> outBoxDtos = new ArrayList<>();
        payments.forEach(p->{
            OutBoxDto outBoxPayment = OutBoxDto.builder()
                    .payload(BaseUtils.convertObjectToString(objectMapper, paymentMapper.toDto(p)))
                    .topic(notifyTopic)
                    .build();
            outBoxDtos.add(outBoxPayment);
        });

        outboxService.createOutboxes(outBoxDtos);
    }

    private Payment toPayment(BookingDto bookingDto, CreatePaymentDto createPaymentDto, String transactionCode, String ip, Instant expireDate) {
        BaseUtils.validateObject(bookingDto.getId(), "Booking id DTO", false);
        BaseUtils.validateObject(bookingDto.getBookingStatus(), "Booking status DTO", false);
        BaseUtils.validateObject(bookingDto.getTotalAmount(), "Booking total amount DTO", false);
        BaseUtils.validateObject(expireDate, "Expire date", false);

        if (bookingDto.getBookingStatus() != BookingStatus.PENDING) {
            log.warn("Booking {} has invalid status: {}. Expected: PENDING",
                    bookingDto.getId(), bookingDto.getBookingStatus());
            throw new BusinessException("Booking invalid status with id: " + bookingDto.getId());
        }

        return Payment.builder()
                .bookingId(bookingDto.getId())
                .amount(bookingDto.getTotalAmount())
                .paymentStatus(PaymentStatus.PROCESSING_PAID)
                .keycloakId(bookingDto.getKeycloakId())
                .paymentMethod(createPaymentDto.getPaymentMethod())
                .paymentGateway(createPaymentDto.getPaymentGateway())
                .transactionCode(transactionCode)
                .ipAddress(ip)
                .expireDate(expireDate)
                .ownerHotelId(bookingDto.getOwnerHotelId())
                .build();
    }

    private final Map<String, String> TARGET_NOTIFY = Map.of(
            KafkaTopic.PAYMENT_PAID, KafkaTopic.NOTIFICATION_PAYMENT_SUCCESS,
            KafkaTopic.PAYMENT_PAID_FAILED, KafkaTopic.NOTIFICATION_PAYMENT_FAILED
    );

    public <T> T withLocks(String lockPrefix, List<String> ids, Supplier<T> action) {
        BaseUtils.validateListObject(ids, lockPrefix + " IDS", false);
        RLock[] lockArray = ids.stream()
                .distinct()
                .sorted()
                .map(id -> BaseUtils.getLock(redissonClient, lockPrefix, id))
                .toArray(RLock[]::new);

        RLock multiLock = BaseUtils.getMultiLock(redissonClient, Arrays.asList(lockArray));

        boolean locked;
        try {
            locked = multiLock.tryLock(5, 30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Interrupted while acquiring lock ({}) for ids: {}", lockPrefix, ids, e);
            throw new BusinessException("Server busy, try again later");
        }

        if (!locked) {
            log.warn("Could not acquire lock ({}) for ids: {}, will not retry", lockPrefix, ids);
            throw new BusinessException("Server busy, try again later");
        }

        try {
            return action.get();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("When processing ({}) ids: {} appearance error: {}", lockPrefix, ids, e.getMessage(), e);
            throw new BusinessException("Server busy, try again later");
        } finally {
            if (multiLock.isHeldByCurrentThread()) {
                log.info("Lock: {} is released for ({}) ids: {}", multiLock, lockPrefix, ids);
                multiLock.unlock();
            }
        }
    }
}