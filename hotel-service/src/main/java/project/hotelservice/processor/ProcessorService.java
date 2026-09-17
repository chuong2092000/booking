package project.hotelservice.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import project.commondto.dto.booking.outbox.BookingRoomInvDto;
import project.commondto.dto.booking.outbox.ResultBookingsDto;
import project.commondto.exception.BusinessException;
import project.commonutils.BaseBusiness;
import project.commonutils.BaseUtils;
import project.hotelservice.orchestrator.OrchestratorService;
import project.hotelservice.service.impl.ProcessedEventService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;


@Service
@RequiredArgsConstructor
@Slf4j
public class ProcessorService {

    private final ProcessedEventService processedEventService;

    private final OrchestratorService orchestratorService;

    private final RedissonClient redissonClient;

    public void uploadS3FileAwsHotel(String refId, String hotelId) {
        withEventAndResourceLocks("hotel", refId, List.of(hotelId),
                () -> orchestratorService.updloadS3FileAwsHotel(refId, hotelId));
    }

    public void uploadS3FileAwsRoomType(String refId, String roomTypeId) {
        withEventAndResourceLocks("room-type", refId, List.of(roomTypeId),
                () -> orchestratorService.updloadS3FileAwsRoomType(refId, roomTypeId));
    }

    public void processQuantityRoomInventory(String refId, List<ResultBookingsDto> resultBookings, boolean hold) {
        BaseUtils.validateListObject(resultBookings, "Result bookings", false);

        List<BookingRoomInvDto> roomInventories = BaseBusiness.detachRoomInventoriesFromResultBooking(resultBookings);
        List<String> bookingIds = BaseBusiness.detachBookingIdsFromResultBooking(resultBookings);
        List<String> roomInvIds = roomInventories.stream().map(BookingRoomInvDto::getId).toList();

        withEventAndResourceLocks("room-inv", refId, roomInvIds, () -> {
            if (hold) {
                orchestratorService.decreaseInventoryAndSaveEvent(refId, bookingIds, roomInventories);
            } else {
                orchestratorService.increaseInventoryAndSaveEvent(refId, bookingIds, roomInventories);
            }
        });
    }

    private void withEventAndResourceLocks(String resourceType, String refId, List<String> resourceIds, Runnable action) {
        BaseUtils.validateObject(refId, "Ref id", false);
        BaseUtils.validateListObject(resourceIds, resourceType + " ids", false);

        if (processedEventService.existEventByRefId(refId)) {
            log.warn("Event {} already processed, skipping", refId);
            return;
        }

        RLock lockEvent = BaseUtils.getLock(redissonClient, "event", refId);
        RLock[] resourceLocks = resourceIds.stream()
                .distinct()
                .sorted()
                .map(id -> BaseUtils.getLock(redissonClient, resourceType, id))
                .toArray(RLock[]::new);

        List<RLock> locks = new ArrayList<>();
        locks.add(lockEvent);
        locks.addAll(Arrays.asList(resourceLocks));

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