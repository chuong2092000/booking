package project.commondto.dto.hotel.owner.request;

import lombok.*;
import lombok.experimental.SuperBuilder;
import project.commondto.dto.hotel.dto.request.CreateHotelDto;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
@ToString
public class OwnerCreateHotelDto extends CreateHotelDto {
}
