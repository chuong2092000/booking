package project.bookingservice.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingExpirationScheduler {
    private final Scheduler scheduler;

    private static final String ROOM_HOLD_GROUP = "room-hold";
    private static final String PAYMENT_HOLD_GROUP = "payment-hold";

    // ===== ROOM HOLD (17p kể từ khi tạo booking) =====
    public void scheduleRoomHold(String bookingId, Instant expireAt) {
        scheduleJob(ExpireRoomHoldJob.class, ROOM_HOLD_GROUP, bookingId, expireAt);
    }

    public void cancelRoomHold(String bookingId) {
        cancelJob(ROOM_HOLD_GROUP, bookingId);
    }

    // ===== PAYMENT HOLD (theo vnp_ExpireDate) =====
    public void schedulePaymentHold(String bookingId, Instant expireAt) {
        scheduleJob(ExpirePaymentHoldJob.class, PAYMENT_HOLD_GROUP, bookingId, expireAt);
    }

    public void cancelPaymentHold(String bookingId) {
        cancelJob(PAYMENT_HOLD_GROUP, bookingId);
    }

    // ===== Helper dùng chung =====
    private void scheduleJob(Class<? extends Job> jobClass, String group, String bookingId, Instant expireAt) {
        JobDetail jobDetail = JobBuilder.newJob(jobClass)
                .withIdentity("job-" + bookingId, group)
                .usingJobData("bookingId", bookingId)
                .storeDurably(false)
                .build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("trigger-" + bookingId, group)
                .startAt(Date.from(expireAt))
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withMisfireHandlingInstructionFireNow())
                .build();

        try {
            scheduler.scheduleJob(jobDetail, trigger);
            log.info("Scheduled {} job for booking: {} at {}", group, bookingId, expireAt);
        } catch (SchedulerException e) {
            log.error("Failed to schedule {} job for booking: {}", group, bookingId, e);
        }
    }

    private void cancelJob(String group, String bookingId) {
        try {
            JobKey jobKey = new JobKey("job-" + bookingId, group);
            boolean deleted = scheduler.deleteJob(jobKey);
            log.info("{} {} job for booking: {}", deleted ? "Deleted" : "No", group, bookingId);
        } catch (SchedulerException e) {
            log.error("Failed to delete {} job for booking: {}", group, bookingId, e);
        }
    }
}
