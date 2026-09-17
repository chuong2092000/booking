package project.paymentservice.orchestrator;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.commondto.dto.KafkaTopic;
import project.commondto.dto.ProcessedEventDto;
import project.commondto.dto.ProcessedEventStatus;
import project.commondto.dto.payment.*;
import project.commondto.dto.payment.vnpay.IpnRequest;
import project.commondto.dto.payment.vnpay.IpnResponse;
import project.commondto.dto.payment.vnpay.VnPayResponse;
import project.commondto.exception.BusinessException;
import project.commonutils.BaseUtils;
import project.paymentservice.client.PaymentGatewayClient;
import project.paymentservice.entity.Payment;
import project.paymentservice.mapper.PaymentMapper;
import project.paymentservice.repo.PaymentRepo;
import project.paymentservice.service.core.CorePaymentService;
import project.paymentservice.service.impl.ProcessedEventService;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;

import static project.commonutils.BaseUtils.*;
import static project.paymentservice.utils.VnPayUtils.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrchestratorService {

    private final PaymentGatewayClient paymentGatewayClient;

    private final ProcessedEventService processedEventService;

    private final ObjectMapper objectMapper;

    private final CorePaymentService corePaymentService;

    private final PaymentRepo paymentRepo;

    private final PaymentMapper paymentMapper;

    @Transactional
    public void callRefundPayment(String refId, PaymentDto paymentDto) {
        String refundedTypeFull = "02";

        BaseUtils.validateObject(paymentDto, "Payment DTO", false);
        BaseUtils.validateObject(refId, "Ref id", false);

        Map<String, Object> paramsRefundPaymentVnPay = createParamsRefundPaymentVnPay(
                paymentDto.getTransactionCode(),
                paymentDto.getPaidAt(),
                paymentDto.getAmount(),
                refundedTypeFull,
                "system",
                paymentDto.getIpAddress());

        executeAndTrackEvent(refId, "REFUND_PAYMENT", paramsRefundPaymentVnPay, () -> {
            VnPayResponse vnPayResponse = paymentGatewayClient.refundPayment(paramsRefundPaymentVnPay);

            boolean verifyRefundResponseHash = verifyRefundResponseHash(vnPayResponse);
            if (!verifyRefundResponseHash) {
                log.error("VNPay refund hash mismatch. TxnRef={}, response={}",
                        paymentDto.getTransactionCode(), vnPayResponse);
                throw new BusinessException("Invalid data, hash secure not matches!");
            }
            log.info("Refunded: transaction code: {}, Info: {} , Code: {}!", vnPayResponse.getVnp_TxnRef(), vnPayResponse.getVnp_OrderInfo(), vnPayResponse.getVnp_ResponseCode());
            if (!"00".equals(vnPayResponse.getVnp_ResponseCode())) {
                corePaymentService.updatePaymentRefundedFailed(paymentDto.getId(), vnPayResponse.getVnp_ResponseCode());
                throw new BusinessException("Refund failed with code: " + vnPayResponse.getVnp_ResponseCode());
            }
        });
    }

    @Transactional
    public void cancelBookingForRefundPayment(String refId, List<String> paymentIds) {
        BaseUtils.validateObject(refId, "Ref id", false);
        BaseUtils.validateListObject(paymentIds, "Payment IDS", false);

        executeAndTrackEvent(refId, "CANCEL_BOOKING", paymentIds, () -> {
            List<PaymentDto> paymentsDto = corePaymentService.processRefundPayment(paymentIds)
                    .stream()
                    .map(paymentMapper::toDto)
                    .toList();
            corePaymentService.createOutboxRefund(paymentsDto);
        });
    }

    @Transactional
    public void processRefundPayment(List<String> paymentIds) {
        BaseUtils.validateListObject(paymentIds, "Payment IDS", false);
        List<PaymentDto> paymentsDto = corePaymentService.processRefundPayment(paymentIds)
                .stream()
                .map(paymentMapper::toDto)
                .toList();
        corePaymentService.createOutboxRefund(paymentsDto);
    }

    @Transactional
    public void updatePaidCashPayment(String paymentId) {
        BaseUtils.validateObject(paymentId, "Payment id", false);
        corePaymentService.updatePaidCashPayment(paymentId);
    }

    @Transactional
    public IpnResponse processPaidOrFailedPayment(IpnRequest ipnRequest, String txnRef) {
        BaseUtils.validateObject(txnRef, "Ref id", false);
        BaseUtils.validateObject(ipnRequest, "Ipn request", false);

        return executeAndTrackEvent(txnRef, "PROCESS_PAYMENT", ipnRequest, () -> {
            List<Payment> payments = corePaymentService.getPaymentsByTransactionCode(txnRef);
            if (payments.isEmpty()) {
                log.warn("[VNPAY IPN] Order not found, txnRef={}", txnRef);
                return IpnResponse.orderNotFound();
            }

            BigDecimal vnpAmount = BigDecimal.valueOf(Long.parseLong(ipnRequest.getVnp_Amount()))
                    .divide(BigDecimal.valueOf(100));
            BigDecimal amount = payments.stream()
                    .filter(o -> o.getPaymentStatus() == PaymentStatus.PROCESSING_PAID)
                    .map(Payment::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            if (amount.compareTo(vnpAmount) != 0) {
                log.warn("[VNPAY IPN] Invalid amount, txnRef={}, expected={}, actual={}", txnRef, amount, vnpAmount);
                return IpnResponse.invalidAmount();
            }

            boolean alreadyConfirmed = payments.stream().anyMatch(Payment::isConfirmed);
            if (alreadyConfirmed) {
                log.info("[VNPAY IPN] Order already confirmed, txnRef={}", txnRef);
                return IpnResponse.orderAlreadyConfirmed();
            }

            String responseCode = ipnRequest.getVnp_ResponseCode();
            String transactionStatus = ipnRequest.getVnp_TransactionStatus();

            if ("00".equals(responseCode) && "00".equals(transactionStatus)) {
                payments.forEach(p -> {
                    p.setPaymentStatus(PaymentStatus.PAID);
                    p.setPaidAt(parseYyyyMMddHHMmSstoInstant(ipnRequest.getVnp_PayDate()));
                    p.setRawGatewayResponse(convertObjectToString(objectMapper, ipnRequest));
                    p.setConfirmed(true);
                });
                corePaymentService.createOutboxPaidOrFailed(payments, KafkaTopic.PAYMENT_PAID);
                corePaymentService.publishOutboxNotifyPayment(payments, KafkaTopic.PAYMENT_PAID);
            } else {
                payments.forEach(p -> {
                    p.setPaymentStatus(PaymentStatus.PAID_FAILED);
                    p.setFailureReason(responseCode);
                    p.setRawGatewayResponse(convertObjectToString(objectMapper, ipnRequest));
                    p.setConfirmed(true);
                });
                corePaymentService.createOutboxPaidOrFailed(payments, KafkaTopic.PAYMENT_PAID_FAILED);
                corePaymentService.publishOutboxNotifyPayment(payments, KafkaTopic.PAYMENT_PAID_FAILED);
            }

            return IpnResponse.success();
        });
    }

    @Transactional
    public void createPaymentCash(List<String> bookingIds) {
        BaseUtils.validateListObject(bookingIds, "Booking IDS", false);

        validateNoActivePayment(bookingIds);

        CreatePaymentDto createPaymentDto = CreatePaymentDto.builder()
                .bookingIds(bookingIds)
                .paymentMethod(PaymentMethod.CASH)
                .paymentGateway(PaymentGateway.MANUAL)
                .build();
        ZonedDateTime expireDate = getZoneDateTimeNow().plusMinutes(15);

        String transactionCode = UUID.randomUUID().toString();
        List<Payment> payments = corePaymentService.createPayments(createPaymentDto, transactionCode, "0.0.0.0", expireDate.toInstant());
        corePaymentService.createOutboxProcessingPaid(payments.stream().map(paymentMapper::toDto).toList());
    }

    @Transactional
    public String createPaymentOnline(CreatePaymentDto createPaymentDto, String ipAddress) {
        BaseUtils.validateObject(createPaymentDto, "Create payment DTO", false);
        BaseUtils.validateListObject(createPaymentDto.getBookingIds(), "Booking IDS", false);
        BaseUtils.validateObject(createPaymentDto.getPaymentMethod(), "Payment method", false);
        BaseUtils.validateObject(createPaymentDto.getPaymentGateway(), "Payment gateway", false);
        BaseUtils.validateObject(ipAddress, "Ip Address", false);

        boolean checkMethodValid = EnumSet.of(PaymentMethod.CASH)
                .contains(createPaymentDto.getPaymentMethod());

        if (checkMethodValid) {
            log.warn("Payment method in func invalid: {}", createPaymentDto.getPaymentMethod());
            throw new BusinessException("Payment method in func invalid: " + createPaymentDto.getPaymentMethod());
        }
        Instant now = BaseUtils.getInstantNow();

        List<Payment> paymentsCheck = paymentRepo.getPaymentsByBookingIdsAndIsDeleted(createPaymentDto.getBookingIds(), false)
                .stream()
                .filter(p -> !p.getPaymentMethod().equals(PaymentMethod.CASH)
                        && p.getPaymentStatus() == PaymentStatus.PROCESSING_PAID
                        && now.isBefore(p.getExpireDate()))
                .toList();

        if (!paymentsCheck.isEmpty()) {
            log.warn("One or more bookings already have a processing online payment: {}",
                    paymentsCheck.stream().map(Payment::getId).toList());
            throw new BusinessException("One or more bookings already have a processing online payment");
        }

        String transactionCode = UUID.randomUUID().toString();
        BigDecimal totalAmount = BigDecimal.ZERO;
        ZonedDateTime expireDate = getZoneDateTimeNow().plusMinutes(15);
        String expireDateStr = getTimeStr(expireDate);

        String orderInfo = "Pay with txnRef: " + transactionCode;
        List<Payment> payments = corePaymentService.createPayments(createPaymentDto, transactionCode, ipAddress, expireDate.toInstant());

        List<PaymentDto> paymentsDto = payments
                .stream()
                .map(paymentMapper::toDto)
                .toList();
        corePaymentService.createOutboxProcessingPaid(paymentsDto);

        for (PaymentDto payment : paymentsDto) {
            BigDecimal amount = payment.getAmount();
            if (amount != null) {
                totalAmount = totalAmount.add(amount);
            }
        }

        return createUrlPaymentVNPay(totalAmount, transactionCode, orderInfo, ipAddress, expireDateStr);
    }

    private void validateNoActivePayment(List<String> bookingIds) {
        List<Payment> activePayments = paymentRepo.getPaymentsByBookingIdsAndIsDeleted(bookingIds, false)
                .stream()
                .filter(p -> p.getPaymentStatus() == PaymentStatus.PROCESSING_PAID
                        || p.getPaymentStatus() == PaymentStatus.PAID)
                .toList();

        if (!activePayments.isEmpty()) {
            List<String> ids = activePayments.stream().map(Payment::getId).toList();
            log.warn("One or more bookings already have an active payment: {}", ids);
            throw new BusinessException("One or more bookings already have an active payment");
        }
    }

    private <T> T executeAndTrackEvent(String refId, String type, Object payload, Callable<T> action) {
        ProcessedEventDto processedEventDto = ProcessedEventDto.builder()
                .refId(refId)
                .type(type)
                .build();
        try {
            T result = action.call();

            processedEventDto.setStatus(ProcessedEventStatus.SUCCESS);
            processedEventDto.setValue(objectMapper.valueToTree(payload));

            ProcessedEventDto successEvent = processedEventService.createSuccessEvent(processedEventDto);
            log.info("Event success created with id: {} and refId: {}", successEvent.getId(), successEvent.getRefId());
            return result;
        } catch (Exception e) {
            processedEventDto.setStatus(ProcessedEventStatus.FAILED);
            processedEventDto.setValue(objectMapper.valueToTree(payload));
            processedEventDto.setErrorMessage(e.getMessage());

            ProcessedEventDto failedEvent = processedEventService.createFailedEvent(processedEventDto);
            log.error("Event failed created with id: {} and refId: {}, error: {}",
                    failedEvent.getId(), failedEvent.getRefId(), failedEvent.getErrorMessage());
            throw new BusinessException(e.getMessage());
        }
    }

    private void executeAndTrackEvent(String refId, String type, Object payload, Runnable action) {
        executeAndTrackEvent(refId, type, payload, () -> {
            action.run();
            return null;
        });
    }
}
