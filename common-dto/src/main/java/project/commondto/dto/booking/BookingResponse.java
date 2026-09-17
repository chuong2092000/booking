package project.commondto.dto.booking;

import lombok.*;
import lombok.experimental.SuperBuilder;
import project.commondto.dto.hotel.UpdateRoomTypeInventoryDto;
import project.commondto.dto.hotel.internal.InternalFullRoomTypeDto;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@SuperBuilder
@ToString
public class BookingResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private BookingDto common;
    private List<InternalFullRoomTypeDto> details;
}