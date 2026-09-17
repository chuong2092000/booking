package project.paymentservice.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import project.commondto.dto.payment.PaymentDto;
import project.commondto.dto.payment.outbox.BookingPaymentDto;
import project.commondto.dto.payment.vnpay.IpnRequest;
import project.commondto.dto.payment.vnpay.IpnResponse;
import project.commondto.exception.BusinessException;
import project.commonutils.BaseUtils;
import project.paymentservice.entity.Payment;
import project.paymentservice.orchestrator.OrchestratorService;
import project.paymentservice.service.core.CorePaymentService;
import project.paymentservice.service.impl.ProcessedEventService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static project.paymentservice.utils.VnPayUtils.verifyIpnRequestHash;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProcessorService {

    private final ProcessedEventService processedEventService;

    private final RedissonClient redissonClient;

    private final OrchestratorService orchestratorService;

    private final CorePaymentService corePaymentService;

    public void refundPayment(String refId, PaymentDto paymentDto) {
        BaseUtils.validateObject(paymentDto, "Payment DTO", false);

        List<String> paymentIds = new ArrayList<>();
        paymentIds.add(paymentDto.getId());
        withEventAndPaymentLocks(refId, paymentIds, () -> orchestratorService.callRefundPayment(refId, paymentDto));
    }

    public void cancelBookingForRefundPayment(String refId, List<BookingPaymentDto> bookingPaymentDtos) {
        BaseUtils.validateObject(refId, "Ref id", false);
        BaseUtils.validateListObject(bookingPaymentDtos, "Bookings payment", false);

        List<String> paymentIds = bookingPaymentDtos.stream().map(BookingPaymentDto::getPaymentId).toList();
        withEventAndPaymentLocks(refId, paymentIds, () -> orchestratorService.cancelBookingForRefundPayment(refId, paymentIds));
    }

    public IpnResponse processIpnPayment(IpnRequest ipnRequest) {
        BaseUtils.validateObject(ipnRequest, "IPN request", false);

        String txnRef = ipnRequest.getVnp_TxnRef();
        BaseUtils.validateObject(txnRef, "Transaction code", false);

        if (!verifyIpnRequestHash(ipnRequest)) {
            log.warn("[VNPAY IPN] Invalid checksum, txnRef={}", txnRef);
            return IpnResponse.invalidChecksum();
        }

        List<Payment> paymentsForLockKeys = corePaymentService.getPaymentsByTransactionCode(txnRef);
        if (paymentsForLockKeys.isEmpty()) {
            log.warn("[VNPAY IPN] Order not found, txnRef={}", txnRef);
            return IpnResponse.orderNotFound();
        }

        try {
            IpnResponse response = withEventAndPaymentLocks(
                    txnRef,
                    paymentsForLockKeys.stream().map(Payment::getId).toList(),
                    () -> orchestratorService.processPaidOrFailedPayment(ipnRequest, txnRef)
            );
            return response != null ? response : IpnResponse.orderAlreadyConfirmed();
        } catch (BusinessException e) {
            log.error("[VNPAY IPN] Failed to process, txnRef={}, error={}", txnRef, e.getMessage());
            return IpnResponse.unknownError();
        }
    }

    private <T> T withEventAndPaymentLocks(String refId, List<String> paymentIds, Callable<T> action) {
        BaseUtils.validateObject(refId, "Ref id", false);
        BaseUtils.validateListObject(paymentIds, "Payment IDS", false);

        if (processedEventService.existEventByRefId(refId)) {
            log.warn("Event {} already processed, skipping", refId);
            return null;
        }

        RLock lockEvent = BaseUtils.getLock(redissonClient, "event", refId);
        RLock[] lockPaymentIds = paymentIds.stream()
                .distinct()
                .sorted()
                .map(id -> BaseUtils.getLock(redissonClient, "payment", id))
                .toArray(RLock[]::new);

        List<RLock> locks = new ArrayList<>();
        locks.add(lockEvent);
        locks.addAll(Arrays.asList(lockPaymentIds));
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
                return null;
            }
            return action.call();
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

    private void withEventAndPaymentLocks(String refId, List<String> paymentIds, Runnable action) {
        withEventAndPaymentLocks(refId, paymentIds, () -> {
            action.run();
            return null;
        });
    }
}
