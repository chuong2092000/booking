package project.commondto.dto.hotel.owner;

import lombok.*;
import lombok.experimental.SuperBuilder;
import project.commondto.dto.hotel.dto.RoomTypeDto;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
@ToString
public class OwnerRoomTypeDto extends RoomTypeDto {
    private boolean isApproval;
}
