package project.hotelservice.spec;

import org.springframework.data.jpa.domain.Specification;
import project.hotelservice.entity.HotelImage;

import java.util.List;

public class HotelImageSpec {
    public static Specification<HotelImage> hasHotelIds(List<String> idList) {
        return ((root, query, builder) ->
        {
            if (idList == null || idList.isEmpty()) {
                return builder.conjunction();
            }

            return root.get("hotelId").in(idList);
        }
        );
    }

    public static Specification<HotelImage> isDeleted(Boolean isDeleted) {
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
