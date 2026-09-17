package project.commonutils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.util.Assert;
import project.commondto.dto.BaseResponse;
import project.commondto.dto.DataResponse;
import project.commondto.exception.BusinessException;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
public abstract class BaseUtils {
    private static final ZoneId VN_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    public static String extractKeycloakUserId(SecurityContext securityContext) {
        Authentication auth = securityContext.getAuthentication();
        if (auth != null && auth.getPrincipal() != null) {
            return (String) auth.getPrincipal();
        }
        return "Anonymous";
    }

    public static String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");

        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        if ("0:0:0:0:0:0:0:1".equals(ip)) {
            return "127.0.0.1";
        }

        return ip;
    }

    public static RLock getMultiLock(RedissonClient redissonClient,
                                     String context,
                                     List<String> values) {
        Assert.notNull(redissonClient, "Redis cannot be null");
        Assert.notNull(context, "Context cannot be null");
        Assert.notEmpty(values, "Values cannot be empty");

        RLock[] locks = values.stream()
                .filter(id -> id != null && !id.isBlank())
                .distinct()
                .sorted()
                .map(id -> redissonClient.getLock("lock:" + context + ":" + id.trim()))
                .toArray(RLock[]::new);

        Assert.notEmpty(locks, "No valid locks generated after filtering");
        return redissonClient.getMultiLock(locks);
    }

    public static RLock getMultiLock(RedissonClient redissonClient,
                                     List<RLock> locks) {
        Assert.notNull(redissonClient, "Redis cannot be null");
        Assert.notEmpty(locks, "No valid locks generated after filtering");
        return redissonClient.getMultiLock(locks.toArray(new RLock[0]));
    }

    public static RLock getLock(RedissonClient redissonClient,
                                String context,
                                String value) {
        Assert.notNull(redissonClient, "Redis cannot be null");
        Assert.notNull(context, "Context cannot be null");
        Assert.notNull(value, "Value cannot be empty");

        RLock lock = redissonClient.getLock("lock:" + context + ":" + value.trim());

        Assert.notNull(lock, "No valid lock generated after filtering");
        return lock;
    }

    public static ResponseEntity<?> baseResponse(String message, HttpStatus status) {
        BaseResponse response = new BaseResponse();
        response.setMessage(message);
        response.setStatus(status.value());
        return new ResponseEntity<>(response, status);
    }

    public static <T> ResponseEntity<?> dataResponse(String message, T data, HttpStatus status) {
        DataResponse<T> response = new DataResponse<>();
        response.setMessage(message);
        response.setStatus(status.value());
        response.setData(data);
        return new ResponseEntity<>(response, status);
    }

    public static void validateObject(String value, String context, boolean manual) {
        validateContextError(context);
        if (value == null || value.isBlank()) {
            setMessageException(context, manual);
        }
    }

    public static <T> void validateObject(T value, String context, boolean manual) {
        validateContextError(context);
        if (value == null) {
            setMessageException(context, manual);
        }
    }

    public static void validateListObject(List<?> value, String context, boolean manual) {
        validateContextError(context);
        if (value == null || value.isEmpty()) {
            setMessageException(context, manual);
        }
    }

    private static void validateContextError(String context) {
        if (context == null || context.isBlank()) {
            log.warn("Context error is null");
            throw new BusinessException("Context error cannot be null");
        }
    }

    private static void setMessageException(String message, boolean manual) {
        validateContextError(message);
        if (manual) {
            log.warn(message);
            throw new BusinessException(message);
        } else {
            log.warn("{} is null", message);
            throw new BusinessException(message + " cannot be null");
        }
    }

    public static <T> String convertObjectToString(ObjectMapper objectMapper, T object) {
        validateObject(objectMapper, "Object mapper", false);
        validateObject(object, "Object", false);
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("Error write value: {}", e.getMessage());
            throw new BusinessException("Error write value string");
        }
    }

    public static ZonedDateTime getZoneDateTimeNow(){
        return ZonedDateTime.now(VN_ZONE);
    }

    public static ZonedDateTime getZoneDateTimeFromInstant(Instant instant){
        return instant.atZone(VN_ZONE);
    }

    public static Instant getInstantNow(){
        return Instant.now();
    }

    public static Instant parseYyyyMMddHHMmSstoInstant(String date) {
        LocalDateTime localDateTime = LocalDateTime.parse(date, DATE_FMT);
        return localDateTime.atZone(VN_ZONE).toInstant();
    }

    public static String getTimeStr(ZonedDateTime zonedDateTime){
        return zonedDateTime.format(DATE_FMT);
    }


    public static <T> T convertStringToObject(ObjectMapper objectMapper, String message, TypeReference<T> typeReference) {
        validateObject(objectMapper, "Object mapper", false);
        validateObject(message, "Message", false);
        validateObject(typeReference, "Type reference", false);

        try {
            return objectMapper.readValue(message, typeReference);
        } catch (JsonProcessingException e) {
            log.error("Error parsing string to object of type [{}]: {}", typeReference.getType(), e.getMessage(), e);
            throw new BusinessException("Error parse value string to object");
        }
    }

}
