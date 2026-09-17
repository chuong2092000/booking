package project.hotelservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.SuperBuilder;
import project.commonutils.entity.core.CommonEntity;

@Entity
@Table(name = "room_type")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RoomType extends CommonEntity {

    @Column(name = "hotel_id", nullable = false)
    private String hotelId;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "image_uri", nullable = false)
    private String imageUri;

    @Column(name = "image_name", nullable = false)
    private String imageName;

    @Column(name = "is_approval", nullable = false)
    @Builder.Default
    private boolean isApproval = false;

    @Column(name = "image_byte")
    private byte[] imageByte;

    @Column(name = "is_processed_im", nullable = false)
    @Builder.Default
    private boolean isProcessedIm = false;
    public String getUrlImage() {
        return this.imageUri + "/" + this.imageName;
    }
}
