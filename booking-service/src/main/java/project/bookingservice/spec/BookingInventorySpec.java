package project.bookingservice.spec;

import org.springframework.data.jpa.domain.Specification;
import project.bookingservice.entity.BookingInventory;

public class BookingInventorySpec {
    public static Specification<BookingInventory> hasBookingDetailId(String bookingDetailId) {
        return ((root, query, builder) ->
        {
            if (bookingDetailId == null) {
                return builder.conjunction();
            }

            return builder.equal(root.get("bookingDetailId"), bookingDetailId);
        }
        );
    }

    public static Specification<BookingInventory> isDeleted(Boolean isDeleted) {
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
