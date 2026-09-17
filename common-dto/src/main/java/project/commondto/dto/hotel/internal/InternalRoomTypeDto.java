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
@ToString
@SuperBuilder
public class InternalRoomTypeDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String id;
    private String name;
    private String urlImage;
    private List<InternalRoomInventoryDto> inventories;
}