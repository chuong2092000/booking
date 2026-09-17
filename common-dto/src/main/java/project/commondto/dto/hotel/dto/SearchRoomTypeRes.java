package project.commondto.dto.hotel.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
@ToString
public class SearchRoomTypeRes extends RoomTypeDto {
    private Integer availableQuantity;
    private BigDecimal price;
}