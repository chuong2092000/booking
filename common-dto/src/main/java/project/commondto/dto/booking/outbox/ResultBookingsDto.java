package project.commondto.dto.booking.outbox;


import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@SuperBuilder
@ToString
public class ResultBookingsDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String bookingId;
    private List<BookingRoomInvDto> inventories;
}