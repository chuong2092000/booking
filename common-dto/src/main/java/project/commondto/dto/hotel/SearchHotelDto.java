package project.commondto.dto.hotel;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@ToString
public class SearchHotelDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String name;
    private String address;
}
