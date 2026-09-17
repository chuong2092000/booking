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
public class ProcessedEventDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String id;
    private String refId;
    private Object value;
    private String type;
    private ProcessedEventStatus status;
    private String errorMessage;
}