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
public class BookingInventoryDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String id;
    private String roomInventoryId;
    private LocalDate date;
    private Integer allocatedQuantity;
    private InventoryStatus status;
}
