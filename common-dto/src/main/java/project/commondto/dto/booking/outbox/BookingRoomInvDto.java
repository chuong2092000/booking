package project.commondto.dto.booking.outbox;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@SuperBuilder
@ToString
public class BookingRoomInvDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String id;
    private Integer quantity;
}
