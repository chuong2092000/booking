package project.embeddingservice.client.dto.hotel;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateRoomTypeInventoryDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String roomTypeId;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Integer quantity;
}
