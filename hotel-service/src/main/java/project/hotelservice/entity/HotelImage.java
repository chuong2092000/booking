package project.hotelservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.SuperBuilder;
import project.commonutils.entity.core.CommonEntity;

@Entity
@Table(name = "hotel_image")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class HotelImage extends CommonEntity {

    @Column(name = "hotel_id", nullable = false)
    private String hotelId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "uri", nullable = false)
    private String uri;

    @Column(name = "is_processed", nullable = false)
    @Builder.Default
    private boolean isProcessed = false;

    @Column(name = "image_byte")
    private byte[] imageByte;
    public String getUrlImage() {
        return this.uri + "/" + this.name;
    }
}
