package project.hotelservice.spec;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import project.hotelservice.entity.RoomInventory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class RoomInventorySpec {
    public static Specification<RoomInventory> hasRoomTypeIds(List<String> idList) {
        return ((root, query, builder) ->
        {
            if (idList == null || idList.isEmpty()) {
                return builder.conjunction();
            }

            return root.get("roomTypeId").in(idList);
        }
        );
    }

    public static Specification<RoomInventory> hasRoomTypeId(String roomTypeId) {
        return ((root, query, builder) ->
        {
            if (roomTypeId == null || roomTypeId.isEmpty()) {
                return builder.conjunction();
            }

            return builder.equal(root.get("roomTypeId"), roomTypeId);
        }
        );
    }

    public static Specification<RoomInventory> hasIds(List<String> idList) {
        return ((root, query, builder) ->
        {
            if (idList == null || idList.isEmpty()) {
                return builder.conjunction();
            }

            return root.get("id").in(idList);
        }
        );
    }

    public static Specification<RoomInventory> isDeleted(Boolean isDeleted) {
        return ((root, query, builder) ->
        {
            if (isDeleted == null) {
                return builder.conjunction();
            }

            return builder.equal(root.get("isDeleted"), isDeleted);
        }
        );
    }

    public static Specification<RoomInventory> isPriceBetween(BigDecimal minPrice, BigDecimal maxPrice) {
        return (root, query, builder) -> {
            if (minPrice == null && maxPrice == null) {
                return builder.conjunction();
            }

            if (minPrice == null) {
                return builder.lessThanOrEqualTo(root.<BigDecimal>get("price"), maxPrice);
            }

            if (maxPrice == null) {
                return builder.greaterThanOrEqualTo(root.<BigDecimal>get("price"), minPrice);
            }

            return builder.between(root.<BigDecimal>get("price"), minPrice, maxPrice);
        };
    }

    public static Specification<RoomInventory> isDateBetween(LocalDate fromDate, LocalDate toDate) {
        return (root, query, builder) -> {
            if (fromDate == null && toDate == null) {
                return builder.conjunction();
            }

            if (fromDate == null) {
                return builder.lessThan(root.get("date"), toDate);
            }

            if (toDate == null) {
                return builder.greaterThanOrEqualTo(root.get("date"), fromDate);
            }

            return builder.and(
                    builder.greaterThanOrEqualTo(root.get("date"), fromDate),
                    builder.lessThan(root.get("date"), toDate)
            );
        };
    }

    public static Specification<RoomInventory> isQuantity(Integer quantity) {
        return (root, query, builder) -> {
            if (quantity == null) {
                return builder.conjunction();
            }
            return builder.greaterThanOrEqualTo(root.get("availableQuantity"), quantity);
        };
    }
}
