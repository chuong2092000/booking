package project.bookingservice.job;

import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;
import project.bookingservice.service.InternalService;

@Component
public class ExpirePaymentHoldJob extends QuartzJobBean {

    @Autowired
    private InternalService internalService;

    @Override
    protected void executeInternal(JobExecutionContext context) {
        String bookingId = context.getMergedJobDataMap().getString("bookingId");
        internalService.cleanBookingPaymentInfo(bookingId);
    }
}