package project.embeddingservice.client.dto.hotel;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchRoomTypesDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String hotelId;
    private String name;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private LocalDate from;
    private LocalDate to;
    private Integer availableQuantity;
}
