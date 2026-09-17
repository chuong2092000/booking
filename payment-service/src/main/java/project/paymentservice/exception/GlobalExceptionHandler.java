package project.paymentservice.exception;

import org.springframework.web.bind.annotation.RestControllerAdvice;
import project.commonutils.exception.CommonExceptionHandler;


@RestControllerAdvice
public class GlobalExceptionHandler extends CommonExceptionHandler {
}