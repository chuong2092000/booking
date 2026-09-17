package project.bookingservice.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import project.bookingservice.entity.Booking;
import project.bookingservice.entity.BookingDetail;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingDetailRepo extends JpaRepository<BookingDetail, String>, JpaSpecificationExecutor<BookingDetail> {
    @Modifying
    @Query("UPDATE BookingDetail o SET o.isDeleted = true WHERE o.bookingId  = :bookingId and o.isDeleted = false")
    void deletedBookingDetailByBookingId(@Param("bookingId") String bookingId);

    @Query("SELECT b.id from BookingDetail b where b.bookingId = :bookingId and b.isDeleted = :isDeleted")
    List<String> findIdsByBookingIdAndIsDeleted(@Param("bookingId") String bookingId, @Param("isDeleted") boolean isDeleted);

    @Query("SELECT b from BookingDetail b where b.id = :bookingDetailId and b.isDeleted = :isDeleted")
    Optional<BookingDetail> findBookingDetailByIdAndIsDeleted(@Param("bookingDetailId") String bookingDetailId, @Param("isDeleted") boolean isDeleted);

    @Query("SELECT b from BookingDetail b where b.bookingId = :bookingId and b.isDeleted = :isDeleted")
    List<BookingDetail> findBookingDetailsByBookingIdAndIsDeleted(@Param("bookingId") String bookingId, @Param("isDeleted") boolean isDeleted);

    @Query("SELECT b from BookingDetail b where b.bookingId in (:bookingIds) and b.isDeleted = :isDeleted")
    List<BookingDetail> findBookingDetailsByBookingIdsIdAndIsDeleted(@Param("bookingIds") List<String> bookingIds, @Param("isDeleted") boolean isDeleted);
}
