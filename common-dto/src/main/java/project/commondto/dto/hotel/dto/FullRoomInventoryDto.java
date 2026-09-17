package project.commondto.dto.hotel.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
@ToString
public class FullRoomInventoryDto extends RoomInventoryDto{
    private Integer reservedQuantity;
    private Integer totalQuantity;
}
