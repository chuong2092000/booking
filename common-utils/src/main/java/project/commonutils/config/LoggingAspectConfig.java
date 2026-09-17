package project.commonutils.config;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;

@Slf4j
public class LoggingAspectConfig {

    public void logBefore(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        log.info("=> Start API: {} | Args: {}", methodName, args);
    }
}
