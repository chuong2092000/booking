package project.commondto.dto.hotel.owner;

import lombok.*;
import lombok.experimental.SuperBuilder;
import project.commondto.dto.hotel.dto.RoomInventoryDto;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
@ToString
public class OwnerFullRoomInventoryDto extends RoomInventoryDto {
    private Integer reservedQuantity;
    private Integer totalQuantity;
}
