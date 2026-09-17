package project.hotelservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.SuperBuilder;
import project.commonutils.entity.core.CommonEntity;

@Entity
@Table(name = "room_type_image")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RoomTypeImage extends CommonEntity {

    @Column(name = "room_type_id", nullable = false)
    private String roomTypeId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "uri", nullable = false)
    private String uri;

    @Column(name = "image_byte")
    private byte[] imageByte;

    @Column(name = "is_processed", nullable = false)
    @Builder.Default
    private boolean isProcessed = false;
    public String getUrlImage() {
        return this.uri + "/" + this.name;
    }
}
