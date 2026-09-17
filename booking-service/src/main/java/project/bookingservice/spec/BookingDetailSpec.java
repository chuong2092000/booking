package project.bookingservice.spec;

import org.springframework.data.jpa.domain.Specification;
import project.bookingservice.entity.BookingDetail;

public class BookingDetailSpec {
    public static Specification<BookingDetail> hasBookingId(String bookingId) {
        return ((root, query, builder) ->
        {
            if (bookingId == null) {
                return builder.conjunction();
            }

            return builder.equal(root.get("bookingId"), bookingId);
        }
        );
    }

    public static Specification<BookingDetail> isDeleted(Boolean isDeleted) {
        return ((root, query, builder) ->
        {
            if (isDeleted == null) {
                return builder.conjunction();
            }

            return builder.equal(root.get("isDeleted"), isDeleted);
        }
        );
    }
}
