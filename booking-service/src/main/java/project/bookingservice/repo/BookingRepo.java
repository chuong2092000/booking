package project.bookingservice.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import project.bookingservice.entity.Booking;
import project.commondto.dto.booking.BookingStatus;
import project.commondto.dto.payment.PaymentStatus;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepo extends JpaRepository<Booking, String>, JpaSpecificationExecutor<Booking> {
    Optional<Booking> getBookingByIdAndIsDeleted(String bookingId, boolean isDeleted);

    Optional<Booking> getBookingByIdAndKeycloakIdAndIsDeleted(String bookingId, String userId, boolean isDeleted);

    Optional<Booking> getBookingByKeycloakIdAndIsDeleted(String userId, boolean isDeleted);

    Optional<Booking> getBookingByIdAndOwnerHotelIdAndIsDeleted(String bookingId, String ownerHotelId, boolean isDeleted);

    boolean existsByIdAndKeycloakIdAndIsDeleted(String bookingId, String userId, boolean isDeleted);

    boolean existsByIdAndOwnerHotelIdAndIsDeleted(String bookingId, String ownerHotelId, boolean isDeleted);

    boolean existsByIdAndIsDeleted(String bookingId, boolean isDeleted);
    Optional<Booking> getBookingByCodeAndIsDeleted(String code, boolean isDeleted);
    Optional<Booking> getBookingByCodeAndOwnerHotelIdAndIsDeleted(String code, String ownerHotelId,boolean isDeleted);
    boolean existsByCode(String code);
    @Modifying
    @Query("UPDATE Booking o SET o.bookingStatus = :status WHERE o.id  = :id and o.keycloakId = :userId and o.isDeleted = false ")
    int updateBookingStatusByIdAndUserId(@Param("id") String id, @Param("status") BookingStatus status, @Param("userId") String userId);

    @Modifying
    @Query("UPDATE Booking o SET o.isDeleted = true WHERE o.id  = :id and o.bookingStatus in (:statuses) and o.isDeleted = false")
    int deletedBooking(@Param("id") String bookingId, @Param("statuses") List<BookingStatus> statuses);

    @Modifying
    @Query("UPDATE Booking o SET o.bookingStatus = :status WHERE o.id  = :id and o.isDeleted = false ")
    int updateBookingStatus(@Param("id") String bookingId, @Param("status") BookingStatus status);

    @Query("SELECT o FROM Booking o WHERE o.id in (:bookingIds) " +
            "and o.isDeleted = :isDeleted " +
            "and o.paymentStatus = :paymentStatus " +
            "and o.bookingStatus = :bookingStatus")
    List<Booking> getBookingsByIdWithStatus(
            @Param("bookingIds") List<String> bookingIds,
            @Param("isDeleted") boolean isDeleted,
            @Param("paymentStatus") PaymentStatus paymentStatus,
            @Param("bookingStatus") BookingStatus bookingStatus);
    @Query("SELECT b FROM Booking b WHERE b.id IN :bookingIds AND b.isDeleted = :isDeleted " +
            "AND b.paymentStatus = :paymentStatus AND b.bookingStatus = :bookingStatus " +
            "AND b.createdDate < :threshold")
    List<Booking> getBookingsByIdWithStatusBeforeThreshold(
            @Param("bookingIds") List<String> bookingIds,
            @Param("isDeleted") boolean isDeleted,
            @Param("paymentStatus") PaymentStatus paymentStatus,
            @Param("bookingStatus") BookingStatus bookingStatus,
            @Param("threshold") Instant threshold);
    @Query("SELECT o FROM Booking o WHERE o.id in (:bookingIds) and o.isDeleted = :isDeleted")
    List<Booking> getBookingsByIdAndIsDeleted(@Param("bookingIds") List<String> bookingIds, @Param("isDeleted") boolean isDeleted);

    @Query("SELECT o FROM Booking o WHERE o.id in (:bookingIds) and o.isDeleted = :isDeleted and o.ownerHotelId = :ownerHotelId")
    List<Booking> getBookingsByIdAndOwnerIdAndIsDeleted(@Param("bookingIds") List<String> bookingIds, @Param("ownerHotelId") String ownerHotelId, @Param("isDeleted") boolean isDeleted);


    @Query("SELECT o FROM Booking o WHERE o.id in (:bookingIds) and o.isDeleted = :isDeleted and o.keycloakId = :userId")
    List<Booking> getBookingsByIdAndUserIdAndIsDeleted(@Param("bookingIds") List<String> bookingIds, @Param("userId") String userId, @Param("isDeleted") boolean isDeleted);


    @Query("SELECT o FROM Booking o WHERE o.paymentId in (:paymentIds) and o.isDeleted = :isDeleted")
    List<Booking> getBookingsByPaymentIdsAndIsDeleted(@Param("paymentIds") List<String> paymentIds, @Param("isDeleted") boolean isDeleted);

}
