package project.commondto.dto.hotel;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@ToString
public class UpdateRoomTypeInventoryDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String roomTypeId;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Integer quantity;
}
