package project.bookingservice.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import project.bookingservice.orchestrator.OrchestratorService;
import project.bookingservice.service.core.CoreBookingService;
import project.bookingservice.service.impl.ProcessedEventService;
import project.commondto.dto.booking.outbox.ResultBookingsDto;
import project.commondto.dto.payment.PaymentDto;
import project.commondto.dto.payment.outbox.BookingPaymentDto;
import project.commondto.exception.BusinessException;
import project.commonutils.BaseUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static project.commonutils.BaseBusiness.detachBookingIdsFromResultBooking;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProcessorService {

    private final OrchestratorService orchestratorService;

    private final ProcessedEventService processedEventService;

    private final RedissonClient redissonClient;

    public void processBookingFailed(String refId, List<ResultBookingsDto> resultBookings) {
        BaseUtils.validateListObject(resultBookings, "Result bookings", false);
        List<String> bookingIds = detachBookingIdsFromResultBooking(resultBookings);

        withEventAndBookingLocks(refId, bookingIds,
                () -> orchestratorService.markBookingFailed(refId, bookingIds));
    }

    public void lockOrReleaseBookingInv(String refId, List<ResultBookingsDto> resultBookings, boolean lock) {
        BaseUtils.validateListObject(resultBookings, "Result bookings", false);
        List<String> bookingIds = detachBookingIdsFromResultBooking(resultBookings);

        withEventAndBookingLocks(refId, bookingIds,
                () -> orchestratorService.lockOrReleaseBookingInv(refId, bookingIds, lock));
    }

    public void processBookingConfirmedAndPaid(String refId, List<BookingPaymentDto> bookingPaymentDtos) {
        BaseUtils.validateListObject(bookingPaymentDtos, "Booking payment DTOS", false);
        List<String> bookingIds = bookingPaymentDtos.stream()
                .map(BookingPaymentDto::getBookingId)
                .distinct()
                .toList();

        withEventAndBookingLocks(refId, bookingIds,
                () -> orchestratorService.markBookingConfirmedAndPaid(refId, bookingPaymentDtos));
    }

    public void processFailedPayment(String refId, List<BookingPaymentDto> bookingPaymentDtos) {
        BaseUtils.validateListObject(bookingPaymentDtos, "Booking payment DTOS", false);
        List<String> bookingIds = bookingPaymentDtos.stream()
                .map(BookingPaymentDto::getBookingId)
                .distinct()
                .toList();

        withEventAndBookingLocks(refId, bookingIds,
                () -> orchestratorService.failedPayment(refId, bookingPaymentDtos));
    }

    public void processPaymentRefundFailed(String refId, List<BookingPaymentDto> bookingPaymentsDto) {
        BaseUtils.validateListObject(bookingPaymentsDto, "Booking payment DTOS", false);
        List<String> bookingIds = bookingPaymentsDto.stream()
                .map(BookingPaymentDto::getBookingId)
                .distinct()
                .toList();

        withEventAndBookingLocks(refId, bookingIds,
                () -> orchestratorService.refundFailedPayment(refId, bookingPaymentsDto));
    }

    public void processPaymentCashPaid(String refId, PaymentDto paymentDto){
        BaseUtils.validateObject(paymentDto, "Payment DTOS", false);
        List<String> bookingIds = List.of(paymentDto.getBookingId());

        withEventAndBookingLocks(refId, bookingIds,
                () -> orchestratorService.paidCashPayment(refId, paymentDto));
    }


    public void processAddPaymentInfo(String refId, List<BookingPaymentDto> bookingPaymentsDto) {
        BaseUtils.validateListObject(bookingPaymentsDto, "Booking payment DTOS", false);
        List<String> bookingIds = bookingPaymentsDto.stream()
                .map(BookingPaymentDto::getBookingId)
                .distinct()
                .toList();

        withEventAndBookingLocks(refId, bookingIds,
                () -> orchestratorService.addPaymentInfoBooking(refId, bookingPaymentsDto));
    }

    public void processPaymentRefunded(String refId, List<BookingPaymentDto> bookingPaymentsDto) {
        BaseUtils.validateListObject(bookingPaymentsDto, "Booking payment DTOS", false);
        List<String> bookingIds = bookingPaymentsDto.stream()
                .map(BookingPaymentDto::getBookingId)
                .distinct()
                .toList();

        withEventAndBookingLocks(refId, bookingIds,
                () -> orchestratorService.refundedPayment(refId, bookingPaymentsDto));
    }

    private void withEventAndBookingLocks(String refId, List<String> bookingIds, Runnable action) {
        BaseUtils.validateObject(refId, "Ref id", false);
        BaseUtils.validateListObject(bookingIds, "Booking IDS", false);

        if (processedEventService.existEventByRefId(refId)) {
            log.warn("Event {} already processed, skipping", refId);
            return;
        }

        RLock lockEvent = BaseUtils.getLock(redissonClient, "event", refId);
        RLock[] lockBookings = bookingIds.stream()
                .distinct()
                .sorted()
                .map(id -> BaseUtils.getLock(redissonClient, "booking", id))
                .toArray(RLock[]::new);

        List<RLock> locks = new ArrayList<>();
        locks.add(lockEvent);
        locks.addAll(Arrays.asList(lockBookings));

        RLock multiLock = BaseUtils.getMultiLock(redissonClient, locks);

        boolean locked;
        try {
            locked = multiLock.tryLock(5, 30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Interrupted while acquiring lock for refId: {}", refId, e);
            throw new BusinessException("Server busy, try again later");
        }

        if (!locked) {
            log.warn("Could not acquire lock for refId: {}, will not retry", refId);
            throw new BusinessException("Server busy, try again later");
        }

        try {
            if (processedEventService.existEventByRefId(refId)) {
                log.warn("Event {} already processed, skipping", refId);
                return;
            }
            action.run();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("When processing message refId: {} appearance error: {}", refId, e.getMessage(), e);
            throw new BusinessException("Server busy, try again later");
        } finally {
            if (multiLock.isHeldByCurrentThread()) {
                log.info("Lock: {} is released for refId: {}", multiLock, refId);
                multiLock.unlock();
            }
        }
    }
}