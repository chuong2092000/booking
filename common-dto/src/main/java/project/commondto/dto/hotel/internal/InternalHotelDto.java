package project.commondto.dto.hotel.internal;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@SuperBuilder
@ToString
public class InternalHotelDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String id;
    private String keycloakId;
    private String name;
    private String urlImage;
}
