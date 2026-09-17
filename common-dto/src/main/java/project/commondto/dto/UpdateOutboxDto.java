package project.commondto.dto;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class UpdateOutboxDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private boolean isSent;
    private List<String> ids;
}