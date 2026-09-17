package project.commondto.dto.hotel.internal;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@SuperBuilder
@ToString
public class InternalRoomInventoryDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String id;
    private LocalDate date;
    private BigDecimal price;
    private Integer availableQuantity;
}
