package project.bookingservice.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import project.commondto.dto.booking.InventoryStatus;
import project.commonutils.entity.core.CommonEntity;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "booking_inventory")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class BookingInventory extends CommonEntity {

    @Column(name = "booking_detail_id", nullable = false)
    private String bookingDetailId;

    @Column(name = "room_inventory_id", nullable = false)
    private String roomInventoryId;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "allocated_quantity", nullable = false)
    private Integer allocatedQuantity;

    @Column(name = "unit_price", precision = 15, scale = 2, nullable = false)
    private BigDecimal unitPrice;

    @Column(name = "sub_total", precision = 15, scale = 2, nullable = false)
    private BigDecimal subTotal;

    @Column(name = "status", nullable = false)
    @Builder.Default
    @Enumerated(EnumType.STRING)
    private InventoryStatus status = InventoryStatus.NEW;
}
