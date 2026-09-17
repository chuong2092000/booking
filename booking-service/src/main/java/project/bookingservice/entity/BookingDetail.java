package project.bookingservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.SuperBuilder;
import project.commonutils.entity.core.CommonEntity;

import java.time.LocalDate;

@Entity
@Table(name = "booking_detail")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BookingDetail extends CommonEntity {

    @Column(name = "booking_id", nullable = false)
    private String bookingId;

    @Column(name = "room_type_id", nullable = false)
    private String roomTypeId;

    @Column(name = "room_type_name", nullable = false)
    private String roomTypeName;

    @Column(name = "room_type_image_url", nullable = false)
    private String roomTypeImageUrl;
}
