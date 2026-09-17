package project.commondto.dto.hotel.admin;

import lombok.*;
import lombok.experimental.SuperBuilder;
import project.commondto.dto.hotel.dto.HotelDto;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
@ToString
public class AdminHotelDto extends HotelDto {
    private String keycloakId;
    private boolean isApproval;
}