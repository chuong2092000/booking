package project.commondto.dto.hotel.admin;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
@ToString
public class AdminFullHotelDto extends AdminHotelDto{
    private String description;
    private List<AdminHotelImageDto> images;
}