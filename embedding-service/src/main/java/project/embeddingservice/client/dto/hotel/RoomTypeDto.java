package project.embeddingservice.client.dto.hotel;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomTypeDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String id;
    private String hotelId;
    private Double price;
    private LocalDate date;
    private Integer totalQuantity;
    private Integer availableQuantity;
    private Integer reservedQuantity;
}