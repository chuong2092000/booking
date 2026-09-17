package project.bookingservice.job;

import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;
import project.bookingservice.service.InternalService;

import java.util.List;

@Component
public class ExpireRoomHoldJob extends QuartzJobBean {
    @Autowired
    private InternalService internalService;

    @Override
    protected void executeInternal(JobExecutionContext context) {
        String bookingId = context.getMergedJobDataMap().getString("bookingId");
        internalService.cancelBooking(List.of(bookingId));
    }
}
