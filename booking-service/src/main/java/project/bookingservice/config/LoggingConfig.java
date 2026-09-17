package project.bookingservice.config;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import project.commonutils.config.LoggingAspectConfig;

@Component
@Aspect
public class LoggingConfig extends LoggingAspectConfig {
    @Before("execution(* project.bookingservice.controller..*(..))")
    @Override
    public void logBefore(JoinPoint joinPoint) {
        super.logBefore(joinPoint);
    }
}
