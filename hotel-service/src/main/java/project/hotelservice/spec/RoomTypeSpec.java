package project.hotelservice.spec;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import project.hotelservice.entity.RoomType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


public class RoomTypeSpec {

    public static Specification<RoomType> hasIds(List<String> idList) {
        return ((root, query, builder) ->
        {
            if (idList == null || idList.isEmpty()) {
                return builder.conjunction();
            }

            return root.get("id").in(idList);
        }
        );
    }

    public static Specification<RoomType> isApproval(Boolean isApproval) {
        return ((root, query, builder) ->
        {
            if (isApproval == null) {
                return builder.conjunction();
            }

            return builder.equal(root.get("isApproval"), isApproval);
        }
        );
    }

    public static Specification<RoomType> isDeleted(Boolean isDeleted) {
        return ((root, query, builder) ->
        {
            if (isDeleted == null) {
                return builder.conjunction();
            }

            return builder.equal(root.get("isDeleted"), isDeleted);
        }
        );
    }

    public static Specification<RoomType> hasHotelId(String hotelId) {
        return ((root, query, builder) ->
        {
            if (hotelId == null) {
                return builder.conjunction();
            }

            return builder.equal(root.get("hotelId"), hotelId);
        }
        );
    }

    public static Specification<RoomType> hasName(String name) {
        return ((root, query, builder) ->
        {
            if (name == null) {
                return builder.conjunction();
            }

            return builder.equal(root.get("name"), name);
        }
        );
    }


}
