package project.hotelservice.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import project.commonutils.entity.core.CommonEntity;

@Entity
@Table(name = "amenity")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Amenity extends CommonEntity {

    @Column(name = "code",nullable = false)
    private String code;

    @Column(name = "name",nullable = false)
    private String name;

    @Column(name = "icon")
    private String icon;

    @Column(name = "category")
    private String category;

    @Column(name = "apply_to", nullable = false)
    @Enumerated(EnumType.STRING)
    private ApplyAmenity applyTo;

    @Column(name = "is_active",nullable = false)
    @Builder.Default
    private boolean isActive = true;
}
