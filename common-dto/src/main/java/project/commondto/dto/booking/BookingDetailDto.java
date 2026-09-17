package project.commondto.dto.booking;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@SuperBuilder
@ToString
public class BookingDetailDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String id;
    private String bookingId;
    private String roomTypeId;
    private String roomTypeName;
    private String roomTypeImageUrl;
}