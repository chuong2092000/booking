package project.bookingservice.orchestrator;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.bookingservice.entity.Booking;
import project.bookingservice.job.BookingExpirationScheduler;
import project.bookingservice.service.InternalService;
import project.bookingservice.service.core.CoreBookingService;
import project.bookingservice.service.impl.ProcessedEventService;
import project.commondto.dto.ProcessedEventDto;
import project.commondto.dto.ProcessedEventStatus;
import project.commondto.dto.booking.BookingDto;
import project.commondto.dto.booking.BookingInventoryDto;
import project.commondto.dto.payment.PaymentDto;
import project.commondto.dto.payment.PaymentMethod;
import project.commondto.dto.payment.PaymentStatus;
import project.commondto.dto.payment.outbox.BookingPaymentDto;
import project.commondto.exception.BusinessException;
import project.commonutils.BaseUtils;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrchestratorService {

    private final ProcessedEventService processedEventService;

    private final InternalService internalService;

    private final CoreBookingService coreBookingService;

    private final ObjectMapper objectMapper;

    private final BookingExpirationScheduler bookingExpirationScheduler;

    @Transactional
    public void markBookingFailed(String refId, List<String> bookingIds) {
        BaseUtils.validateObject(refId, "Ref id", false);
        BaseUtils.validateListObject(bookingIds, "Booking IDS", false);
        String type = EventType.FAILED_BOOKING.name();

        executeAndTrackEvent(refId, type, bookingIds, () -> {
            List<BookingDto> updatedBooking = internalService.failBooking(bookingIds);
            log.info("Booking updated with ids: {}", updatedBooking.stream().map(BookingDto::getId).toList());
        });
    }

    @Transactional
    public void markBookingConfirmedAndPaid(String refId, List<BookingPaymentDto> bookingPaymentDtos) {
        BaseUtils.validateObject(refId, "Ref id", false);
        BaseUtils.validateListObject(bookingPaymentDtos, "Booking Payment DTOS", false);

        String type = EventType.CONFIRMED_BOOKING.name();
        List<String> bookingIds = bookingPaymentDtos.stream()
                .map(BookingPaymentDto::getBookingId)
                .distinct()
                .toList();

        Instant now = BaseUtils.getInstantNow();

        executeAndTrackEvent(refId, type, bookingIds, () -> {
            List<Booking> bookingsSearch = getBookings(bookingIds);
            List<Booking> bookings = bookingsSearch.stream()
                    .filter(booking -> Duration.between(booking.getCreatedDate(), now).toMinutes() < 15)
                    .toList();

            checkMissingBooking(getMapBooking(bookingsSearch), bookingIds);

            if (bookings.size() != bookingsSearch.size()) {
                List<String> expiredIds = bookingsSearch.stream()
                        .filter(b -> !bookings.contains(b))
                        .map(Booking::getId)
                        .toList();
                log.warn("Some bookings expired, cannot confirm payment: {}", expiredIds);
                throw new BusinessException("Booking(s) expired: " + expiredIds);
            }

            Map<String, Booking> bookingMap = getMapBooking(bookings);

            List<Booking> bookingUpdatedPayment = bookingPaymentDtos.stream().map(dto -> {
                Booking booking = bookingMap.get(dto.getBookingId());
                Booking bookingUpdated = coreBookingService.addPaymentBooking(booking, dto.getPaymentId(),
                        dto.getPaymentGateway(), dto.getPaymentMethod(), PaymentStatus.PAID);
                bookingExpirationScheduler.cancelPaymentHold(dto.getBookingId());
                return bookingUpdated;
            }).toList();

            List<Booking> confirmedBookings = coreBookingService.confirmBookings(bookingUpdatedPayment);
            log.info("Booking updated with ids: {}", confirmedBookings.stream().map(Booking::getId).toList());
        });
    }

    @Transactional
    public void lockOrReleaseBookingInv(String refId, List<String> bookingIds, boolean lock) {
        BaseUtils.validateObject(refId, "Ref id", false);
        BaseUtils.validateListObject(bookingIds, "Booking IDS", false);

        String type = lock ? EventType.LOCK_INVENTORIES.name() : EventType.RELEASE_INVENTORIES.name();
        Instant expireAt = Instant.now().plus(17, ChronoUnit.MINUTES);
        executeAndTrackEvent(refId, type, bookingIds, () -> {
            List<BookingInventoryDto> bookingInventories;
            if (lock) {
                bookingInventories = internalService.lockBookingInventories(bookingIds);
                bookingIds.forEach(b -> bookingExpirationScheduler.scheduleRoomHold(b, expireAt));
            } else {
                bookingInventories = internalService.releaseBookingInventories(bookingIds);
            }
            log.info("Booking inventories updated status with ids: {}",
                    bookingInventories.stream().map(BookingInventoryDto::getId).toList());
        });
    }

    @Transactional
    public void refundFailedPayment(String refId, List<BookingPaymentDto> bookingPaymentsDto) {
        BaseUtils.validateObject(refId, "Ref id", false);
        BaseUtils.validateListObject(bookingPaymentsDto, "Booking payments DTO", false);
        List<String> bookingIds = bookingPaymentsDto.stream()
                .map(BookingPaymentDto::getBookingId)
                .distinct()
                .toList();
        String type = EventType.REFUND_FAILED_PAYMENT.name();

        executeAndTrackEvent(refId, type, bookingPaymentsDto, () -> {
            List<Booking> bookings = getBookings(bookingIds);
            Map<String, Booking> bookingMap = getMapBooking(bookings);
            checkMissingBooking(bookingMap, bookingIds);

            List<Booking> bookingUpdatedPayment = bookingPaymentsDto.stream().map(dto -> {
                Booking booking = bookingMap.get(dto.getBookingId());
                return coreBookingService.updatePaymentBooking(booking,
                        dto.getPaymentGateway(), dto.getPaymentMethod(), PaymentStatus.REFUND_FAILED);
            }).toList();

            log.info("Bookings marked as REFUND_FAILED with ids: {}",
                    bookingUpdatedPayment.stream().map(Booking::getId).toList());
        });
    }

    @Transactional
    public void deleteBooking(String bookingId) {
        BaseUtils.validateObject(bookingId, "Booking id", false);
        coreBookingService.deleteBooking(bookingId);
        bookingExpirationScheduler.cancelPaymentHold(bookingId);
        bookingExpirationScheduler.cancelRoomHold(bookingId);
    }

    @Transactional // API
    public List<Booking> cancelBookings(List<Booking> bookings) {
        BaseUtils.validateListObject(bookings, "Bookings", false);

        bookings.forEach(booking -> {
            bookingExpirationScheduler.cancelRoomHold(booking.getId());
            bookingExpirationScheduler.cancelPaymentHold(booking.getId());
        });

        return coreBookingService.cancelBookings(bookings);
    }

    @Transactional
    public void failedPayment(String refId, List<BookingPaymentDto> bookingPaymentsDto) {
        BaseUtils.validateObject(refId, "Ref id", false);
        BaseUtils.validateListObject(bookingPaymentsDto, "Booking payments DTO", false);
        List<String> bookingIds = bookingPaymentsDto.stream()
                .map(BookingPaymentDto::getBookingId)
                .distinct()
                .toList();
        String type = EventType.FAILED_PAYMENT.name();

        executeAndTrackEvent(refId, type, bookingPaymentsDto, () -> {
            List<Booking> bookings = getBookings(bookingIds);
            Map<String, Booking> bookingMap = getMapBooking(bookings);
            checkMissingBooking(bookingMap, bookingIds);
            Instant expireAt = Instant.now().plus(17, ChronoUnit.MINUTES);
            List<Booking> bookingUpdatedPayment = bookingPaymentsDto.stream().map(dto -> {
                Booking booking = bookingMap.get(dto.getBookingId());
                Booking bookingUpdated = coreBookingService.addPaymentBooking(booking, dto.getPaymentId(),
                        dto.getPaymentGateway(), dto.getPaymentMethod(), PaymentStatus.PAID_FAILED);
                bookingExpirationScheduler.cancelPaymentHold(dto.getBookingId());
                bookingExpirationScheduler.scheduleRoomHold(dto.getBookingId(), expireAt);
                return bookingUpdated;
            }).toList();

            log.info("Bookings payment status set to FAILED with ids: {}",
                    bookingUpdatedPayment.stream().map(Booking::getId).toList());
        });
    }

    @Transactional
    public void addPaymentInfoBooking(String refId, List<BookingPaymentDto> bookingPaymentsDto) {
        BaseUtils.validateObject(refId, "Ref id", false);
        BaseUtils.validateListObject(bookingPaymentsDto, "Booking payments DTO", false);
        List<String> bookingIds = bookingPaymentsDto.stream()
                .map(BookingPaymentDto::getBookingId)
                .distinct()
                .toList();
        String type = EventType.ADD_PAYMENT_INFO.name();

        executeAndTrackEvent(refId, type, bookingPaymentsDto, () -> {
            List<Booking> bookings = getBookings(bookingIds);
            Map<String, Booking> bookingMap = getMapBooking(bookings);
            checkMissingBooking(bookingMap, bookingIds);

            List<Booking> bookingConfirmCash = new ArrayList<>();

            List<Booking> bookingUpdatedPayment = bookingPaymentsDto.stream().map(dto -> {
                Booking booking = bookingMap.get(dto.getBookingId());

                Booking updated = coreBookingService.addPaymentBooking(booking, dto.getPaymentId(),
                        dto.getPaymentGateway(), dto.getPaymentMethod(), PaymentStatus.PROCESSING_PAID);

                if (dto.getPaymentMethod().equals(PaymentMethod.CASH)) {
                    bookingExpirationScheduler.cancelRoomHold(dto.getBookingId());
                    bookingConfirmCash.add(booking);
                } else {
                    bookingExpirationScheduler.cancelRoomHold(dto.getBookingId());
                    bookingExpirationScheduler.schedulePaymentHold(dto.getBookingId(), dto.getExpireDate());
                }

                return updated;
            }).toList();
            if (!bookingConfirmCash.isEmpty()) {
                coreBookingService.confirmBookings(bookingConfirmCash);
            }
            log.info("Bookings payment info added with ids: {}", bookingUpdatedPayment.stream().map(Booking::getId).toList());
        });
    }

    @Transactional
    public void refundedPayment(String refId, List<BookingPaymentDto> bookingPaymentsDto) {
        BaseUtils.validateObject(refId, "Ref id", false);
        BaseUtils.validateListObject(bookingPaymentsDto, "Booking payments DTO", false);
        List<String> bookingIds = bookingPaymentsDto.stream()
                .map(BookingPaymentDto::getBookingId)
                .distinct()
                .toList();
        String type = EventType.REFUNDED_PAYMENT.name();

        executeAndTrackEvent(refId, type, bookingPaymentsDto, () -> {
            List<Booking> bookings = getBookings(bookingIds);
            Map<String, Booking> bookingMap = getMapBooking(bookings);
            checkMissingBooking(bookingMap, bookingIds);

            List<Booking> bookingUpdatedPayment = bookingPaymentsDto.stream().map(dto -> {
                Booking booking = bookingMap.get(dto.getBookingId());
                return coreBookingService.updatePaymentBooking(booking,
                        dto.getPaymentGateway(), dto.getPaymentMethod(), PaymentStatus.REFUNDED);
            }).toList();

            log.info("Bookings marked as REFUNDED with ids: {}",
                    bookingUpdatedPayment.stream().map(Booking::getId).toList());
        });
    }

    @Transactional
    public void paidCashPayment(String refId, PaymentDto paymentDto) {
        BaseUtils.validateObject(refId, "Ref id", false);
        BaseUtils.validateObject(paymentDto, "Payment DTO", false);
        BaseUtils.validateObject(paymentDto.getBookingId(), "Booking id", false);

        if (!PaymentMethod.CASH.equals(paymentDto.getPaymentMethod())) {
            throw new BusinessException("Invalid payment method for cash payment: " + paymentDto.getPaymentMethod());
        }

        List<PaymentDto> paymentDtos = List.of(paymentDto);
        List<String> bookingIds = List.of(paymentDto.getBookingId());
        String type = EventType.PAID_CASH_PAYMENT.name();

        executeAndTrackEvent(refId, type, paymentDtos, () -> {
            List<Booking> bookings = getBookings(bookingIds);
            Map<String, Booking> bookingMap = getMapBooking(bookings);
            checkMissingBooking(bookingMap, bookingIds);

            List<Booking> bookingUpdatedPayment = paymentDtos.stream().map(dto -> {
                Booking booking = bookingMap.get(dto.getBookingId());
                return coreBookingService.updatePaymentBooking(booking,
                        dto.getPaymentGateway(), dto.getPaymentMethod(), PaymentStatus.PAID);
            }).toList();

            log.info("Bookings marked as PAID (cash) with ids: {}",
                    bookingUpdatedPayment.stream().map(Booking::getId).toList());
        });
    }

    private void executeAndTrackEvent(String refId, String type, Object payload, Runnable action) {
        ProcessedEventDto processedEventDto = ProcessedEventDto.builder()
                .refId(refId)
                .type(type)
                .build();

        try {
            action.run();

            processedEventDto.setStatus(ProcessedEventStatus.SUCCESS);
            processedEventDto.setValue(objectMapper.valueToTree(payload));

            ProcessedEventDto successEvent = processedEventService.createSuccessEvent(processedEventDto);
            log.info("Event success created with id: {} and refId: {}", successEvent.getId(), successEvent.getRefId());
        } catch (Exception e) {
            processedEventDto.setStatus(ProcessedEventStatus.FAILED);
            processedEventDto.setValue(objectMapper.valueToTree(payload));
            processedEventDto.setErrorMessage(e.getMessage());

            ProcessedEventDto failedEvent = processedEventService.createFailedEvent(processedEventDto);
            log.error("Event failed created with id: {} and refId: {}, error: {}",
                    failedEvent.getId(), failedEvent.getRefId(), failedEvent.getErrorMessage());
            throw e;
        }
    }

    private List<Booking> getBookings(List<String> bookingIds) {
        BaseUtils.validateListObject(bookingIds, "Booking IDS", false);
        return coreBookingService.getBookings(bookingIds);
    }

    private Map<String, Booking> getMapBooking(List<Booking> bookings) {
        BaseUtils.validateListObject(bookings, "Bookings", false);
        return bookings.stream()
                .collect(Collectors.toMap(Booking::getId, b -> b));
    }

    private void checkMissingBooking(Map<String, Booking> bookingMap, List<String> bookingIds) {
        BaseUtils.validateListObject(bookingIds, "Booking IDS", false);
        List<String> missingIds = bookingIds.stream()
                .filter(id -> !bookingMap.containsKey(id))
                .toList();
        if (!missingIds.isEmpty()) {
            log.warn("Booking not found with ids: {}", missingIds);
            throw new BusinessException("Booking not found with ids: " + missingIds);
        }
    }

    public enum EventType {
        FAILED_BOOKING,
        CONFIRMED_BOOKING,
        LOCK_INVENTORIES,
        RELEASE_INVENTORIES,
        FAILED_PAYMENT,
        ADD_PAYMENT_INFO,
        REFUNDED_PAYMENT,
        REFUND_FAILED_PAYMENT,
        PAID_CASH_PAYMENT
    }
}
