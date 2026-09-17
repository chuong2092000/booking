package project.hotelservice.spec;

import org.springframework.data.jpa.domain.Specification;
import project.hotelservice.entity.RoomTypeImage;

import java.util.List;

public class RoomTypeImageSpec {

    public static Specification<RoomTypeImage> hasRoomTypeIds(List<String> idList) {
        return ((root, query, builder) ->
        {
            if (idList == null || idList.isEmpty()) {
                return builder.conjunction();
            }

            return root.get("roomTypeId").in(idList);
        }
        );
    }

    public static Specification<RoomTypeImage> isDeleted(Boolean isDeleted) {
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
