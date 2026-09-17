package project.commondto.dto.hotel.admin;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
@ToString
public class AdminFullRoomTypeDto extends AdminRoomTypeDto {
    private String description;
    private List<AdminRoomTypeImageDto> images;
    private List<AdminFullRoomInventoryDto> inventories;
}
