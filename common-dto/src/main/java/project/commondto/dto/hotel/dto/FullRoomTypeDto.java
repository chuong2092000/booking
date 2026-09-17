package project.commondto.dto.hotel.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
@ToString
public class FullRoomTypeDto extends RoomTypeDto {
    private String description;
    private List<RoomTypeImageDto> images;
}
