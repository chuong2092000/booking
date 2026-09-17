package project.paymentservice.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import project.commondto.dto.payment.PaymentMethod;
import project.paymentservice.entity.Payment;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepo extends JpaRepository<Payment, String>, JpaSpecificationExecutor<Payment> {
    @Query("SELECT p FROM Payment p WHERE p.id in (:paymentIds) AND p.isDeleted = :isDeleted")
    List<Payment> getPaymentByIdsAndIsDeleted(@Param("paymentIds") List<String> paymentIds, @Param("isDeleted") boolean isDeleted);

    List<Payment> getPaymentsByTransactionCodeAndIsDeleted(String transactionCode, boolean isDeleted);

    Optional<Payment> getPaymentByIdAndIsDeleted(String id, boolean isDeleted);

    Optional<Payment> getPaymentByIdAndPaymentMethodAndIsDeleted(String id, PaymentMethod paymentMethod, boolean isDeleted);

    @Query("SELECT p FROM Payment p WHERE p.bookingId in (:bookingIds) AND p.isDeleted = :isDeleted")
    List<Payment> getPaymentsByBookingIdsAndIsDeleted(@Param("bookingIds") List<String> bookingIds, @Param("isDeleted") boolean isDeleted);
}
