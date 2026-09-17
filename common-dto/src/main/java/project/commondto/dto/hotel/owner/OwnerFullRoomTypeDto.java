package project.commondto.dto.hotel.owner;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
@ToString
public class OwnerFullRoomTypeDto extends OwnerRoomTypeDto {
    private String description;
    private List<OwnerRoomTypeImageDto> images;
    private List<OwnerFullRoomInventoryDto> inventories;
}
