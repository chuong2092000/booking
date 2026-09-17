package project.commondto.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BusinessException extends RuntimeException {

    private String message;
    private Integer status;
    private final Long responseTime = System.currentTimeMillis();

    public BusinessException(String message) {
        this.message = message;
        this.status = 400;
    }

    public BusinessException(String message, Integer status) {
        this.message = message;
        this.status = status;
    }
}