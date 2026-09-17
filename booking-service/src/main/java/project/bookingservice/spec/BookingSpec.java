package project.bookingservice.spec;

import org.springframework.data.jpa.domain.Specification;
import project.bookingservice.entity.Booking;
import project.commondto.dto.booking.BookingStatus;
import project.commondto.dto.payment.PaymentStatus;

public class BookingSpec {
    public static Specification<Booking> isDeleted(Boolean isDeleted) {
        return ((root, query, builder) ->
        {
            if (isDeleted == null) {
                return builder.conjunction();
            }

            return builder.equal(root.get("isDeleted"), isDeleted);
        }
        );
    }

    public static Specification<Booking> hasBookingStatus(BookingStatus status) {
        return ((root, query, builder) ->
        {
            if (status == null) {
                return builder.conjunction();
            }

            return builder.equal(root.get("bookingStatus"), status);
        }
        );
    }

    public static Specification<Booking> hasPaymentStatus(PaymentStatus status) {
        return ((root, query, builder) ->
        {
            if (status == null) {
                return builder.conjunction();
            }

            return builder.equal(root.get("paymentStatus"), status);
        }
        );
    }

    public static Specification<Booking> sortByCreateDate() {
        return ((root, query, builder) ->
        {
            assert query != null;
            query.orderBy(builder.asc(root.get("createdDate")));
            return null;
        }
        );
    }

    public static Specification<Booking> hasKeycloakId(String userId) {
        return ((root, query, builder) ->
        {
            if (userId == null) {
                return builder.conjunction();
            }

            return builder.equal(root.get("keycloakId"), userId);
        }
        );
    }

    public static Specification<Booking> hasHotelId(String hotelId) {
        return ((root, query, builder) ->
        {
            if (hotelId == null) {
                return builder.conjunction();
            }

            return builder.equal(root.get("hotelId"), hotelId);
        }
        );
    }

    public static Specification<Booking> hasPaymentId(String paymentId) {
        return ((root, query, builder) ->
        {
            if (paymentId == null) {
                return builder.conjunction();
            }

            return builder.equal(root.get("paymentId"), paymentId);
        }
        );
    }

    public static Specification<Booking> hasOwnerHotelId(String ownerHotelId) {
        return ((root, query, builder) ->
        {
            if (ownerHotelId == null) {
                return builder.conjunction();
            }

            return builder.equal(root.get("ownerHotelId"), ownerHotelId);
        }
        );
    }
}
