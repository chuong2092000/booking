package project.apigateway.dto;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class BaseResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String message;
    private Long responseTime = System.currentTimeMillis();
    private Integer status;
}