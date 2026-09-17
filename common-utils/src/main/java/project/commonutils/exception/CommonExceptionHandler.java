package project.commonutils.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import project.commondto.dto.BaseResponse;
import project.commondto.dto.ValidationResponse;
import project.commondto.exception.BusinessException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;


@Slf4j
public class CommonExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<BaseResponse> handleBusinessException(BusinessException ex) {
        return buildResponse(ex.getMessage(), HttpStatus.valueOf(ex.getStatus()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationResponse> handleBodyValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();

        ex.getBindingResult().getFieldErrors()
                .forEach(fe -> mergeError(errors, fe.getField(), fe.getDefaultMessage()));

        ex.getBindingResult().getGlobalErrors()
                .forEach(ge -> mergeError(errors, ge.getObjectName(), ge.getDefaultMessage()));

        return buildValidationResponse(errors);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ValidationResponse> handleParamValidation(HandlerMethodValidationException ex) {
        Map<String, String> errors = new LinkedHashMap<>();

        ex.getParameterValidationResults().forEach(result -> {
            String paramName = resolveParamName(result.getMethodParameter());
            result.getResolvableErrors()
                    .forEach(err -> mergeError(errors, paramName, err.getDefaultMessage()));
        });

        return buildValidationResponse(errors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ValidationResponse> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> errors = new LinkedHashMap<>();

        ex.getConstraintViolations().forEach(violation -> {
            String fieldName = parseFieldName(violation.getPropertyPath().toString());
            mergeError(errors, fieldName, violation.getMessage());
        });

        return buildValidationResponse(errors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<BaseResponse> handleUnreadableBody(HttpMessageNotReadableException ex) {
        log.warn("Malformed request body: {}", ex.getMessage());
        return buildResponse("Malformed JSON request", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<BaseResponse> handleAccessDenied(AuthorizationDeniedException ex) {
        return buildResponse("Access denied", HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse> handleUnexpectedException(Exception ex) {
        log.error("Unhandled exception", ex);
        return buildResponse("Internal server error", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private void mergeError(Map<String, String> errors, String key, String message) {
        String value = Objects.requireNonNullElse(message, "Invalid value");
        errors.merge(key, value, (existing, incoming) -> existing + "; " + incoming);
    }

    private String resolveParamName(MethodParameter parameter) {
        String name = parameter.getParameterName();
        return name != null ? name : "param" + parameter.getParameterIndex();
    }

    private String parseFieldName(String propertyPath) {
        int secondDotIndex = propertyPath.indexOf('.', propertyPath.indexOf('.') + 1);
        return secondDotIndex >= 0 ? propertyPath.substring(secondDotIndex + 1) : propertyPath;
    }

    private ResponseEntity<BaseResponse> buildResponse(String message, HttpStatus status) {
        BaseResponse response = new BaseResponse();
        response.setMessage(message);
        response.setStatus(status.value());
        return new ResponseEntity<>(response, status);
    }

    private ResponseEntity<ValidationResponse> buildValidationResponse(Map<String, String> errors) {
        ValidationResponse response = new ValidationResponse();
        response.setMessage("Validation failed");
        response.setErrors(errors);
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

}
