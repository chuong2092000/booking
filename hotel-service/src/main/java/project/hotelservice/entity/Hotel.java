package project.hotelservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.SuperBuilder;
import project.commonutils.entity.core.CommonEntity;


@Entity
@Table(name = "hotel")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Hotel extends CommonEntity {

    @Column(name = "keycloak_id", nullable = false)
    private String keycloakId;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "image_uri", nullable = false)
    private String imageUri;

    @Column(name = "image_name", nullable = false)
    private String imageName;

    @Column(name = "is_processed_im", nullable = false)
    @Builder.Default
    private boolean isProcessedIm = false;

    @Column(name = "is_approval", nullable = false)
    @Builder.Default
    private boolean isApproval = false;

    @Column(name = "image_byte")
    private byte[] imageByte;

    public String getUrlImage() {
        return this.imageUri + "/" + this.imageName;
    }
}
