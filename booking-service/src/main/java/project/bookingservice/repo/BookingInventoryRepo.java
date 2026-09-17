package project.bookingservice.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import project.bookingservice.entity.BookingInventory;

import java.util.List;

@Repository
public interface BookingInventoryRepo extends JpaRepository<BookingInventory, String>, JpaSpecificationExecutor<BookingInventory> {
    @Modifying
    @Query("UPDATE BookingInventory i SET i.isDeleted = true WHERE i.bookingDetailId IN :bookingDetailIds AND i.isDeleted = false AND i.status = 'RELEASED'")
    void deleteByBookingDetailIds(@Param("bookingDetailIds") List<String> bookingDetailIds);

    @Query("SELECT i FROM BookingInventory i WHERE i.bookingDetailId IN :bookingDetailIds AND i.isDeleted = :isDeleted")
    List<BookingInventory> findBookingInventoryByBookingDetailIdsAndIsDeleted(@Param("bookingDetailIds") List<String> bookingDetailIds, @Param("isDeleted") boolean isDeleted);
}
