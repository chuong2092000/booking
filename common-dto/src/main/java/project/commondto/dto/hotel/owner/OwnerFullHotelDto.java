package project.commondto.dto.hotel.owner;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
@ToString
public class OwnerFullHotelDto extends OwnerHotelDto{
    private String description;
    private List<OwnerHotelImageDto> images;
}
