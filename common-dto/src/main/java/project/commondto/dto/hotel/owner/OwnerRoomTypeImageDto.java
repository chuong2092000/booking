package project.commondto.dto.hotel.owner;

import lombok.*;
import lombok.experimental.SuperBuilder;
import project.commondto.dto.hotel.dto.RoomTypeImageDto;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
@ToString
public class OwnerRoomTypeImageDto extends RoomTypeImageDto {
}
