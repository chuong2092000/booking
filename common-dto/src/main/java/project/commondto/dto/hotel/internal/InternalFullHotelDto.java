package project.commondto.dto.hotel.internal;

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
public class InternalFullHotelDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private InternalHotelDto internalHotelDto;
    private List<InternalRoomTypeDto> internalRoomTypesDto;
    private List<InternalRoomInventoryDto> internalInventoriesDto;
}
