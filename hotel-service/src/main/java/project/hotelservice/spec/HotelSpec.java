package project.hotelservice.spec;

import org.springframework.data.jpa.domain.Specification;
import project.hotelservice.entity.Hotel;

import java.util.List;

public class HotelSpec {

    public static Specification<Hotel> hasIds(List<String> idList) {
        return ((root, query, builder) ->
        {
            if (idList == null || idList.isEmpty()) {
                return builder.conjunction();
            }

            return root.get("id").in(idList);
        }
        );
    }



    public static Specification<Hotel> hasId(String id) {
        return ((root, query, builder) ->
        {
            if (id == null) {
                return builder.conjunction();
            }

            return builder.equal(root.get("id"), id);
        }
        );
    }

    public static Specification<Hotel> isDeleted(Boolean isDeleted) {
        return ((root, query, builder) ->
        {
            if (isDeleted == null) {
                return builder.conjunction();
            }

            return builder.equal(root.get("isDeleted"), isDeleted);
        }
        );
    }

    public static Specification<Hotel> isApproval(Boolean isApproval) {
        return ((root, query, builder) ->
        {
            if (isApproval == null) {
                return builder.conjunction();
            }

            return builder.equal(root.get("isApproval"), isApproval);
        }
        );
    }

    public static Specification<Hotel> hasName(String name) {
        return (root, query, builder) -> {
            if (name == null || name.trim().isEmpty()) {
                return builder.conjunction();
            }

            String pattern = "%" + name.toLowerCase() + "%";

            return builder.like(builder.lower(root.get("name")), pattern);
        };
    }

    public static Specification<Hotel> hasAddress(String address) {
        return (root, query, builder) -> {
            if (address == null || address.trim().isEmpty()) {
                return builder.conjunction();
            }

            String pattern = "%" + address.toLowerCase() + "%";

            return builder.like(builder.lower(root.get("address")), pattern);
        };
    }

    public static Specification<Hotel> hasKeycloakId(String userId) {
        return ((root, query, builder) ->
        {
            if (userId == null) {
                return builder.conjunction();
            }

            return builder.equal(root.get("keycloakId"), userId);
        }
        );
    }
}
