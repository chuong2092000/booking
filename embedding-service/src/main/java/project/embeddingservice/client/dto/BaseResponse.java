package project.embeddingservice.client.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BaseResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String message;
    private Long responseTime = System.currentTimeMillis();
    private Integer status;
}