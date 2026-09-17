package project.commondto.dto;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class OutBoxDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String id;
    private String payload;
    private String topic;
    private boolean isSent;
    private Integer retryCount;
    private String lastError;
}
