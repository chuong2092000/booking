package project.commondto.dto.hotel.owner;

import lombok.*;
import lombok.experimental.SuperBuilder;
import project.commondto.dto.hotel.dto.HotelImageDto;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
@ToString
public class OwnerHotelImageDto extends HotelImageDto {
}
