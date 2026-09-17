package project.commondto.dto.hotel.internal;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
@ToString
public class InternalBookingRoomDto extends InternalHotelDto {
    private List<InternalRoomTypeDto> roomTypes;
}