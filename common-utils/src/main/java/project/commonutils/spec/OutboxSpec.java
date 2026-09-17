package project.commonutils.spec;

import org.springframework.data.jpa.domain.Specification;
import project.commonutils.entity.core.OutBoxCM;

public class OutboxSpec {

    public static <T extends OutBoxCM> Specification<T> isDeleted(Boolean isDeleted) {
        return (root, query, builder) -> {
            if (isDeleted == null) {
                return builder.conjunction();
            }

            return builder.equal(root.get("isDeleted"), isDeleted);
        };
    }

    public static <T extends OutBoxCM> Specification<T> isSent(Boolean isSent) {
        return ((root, query, builder) ->
        {
            if (isSent == null) {
                return builder.conjunction();
            }

            return builder.equal(root.get("isSent"), isSent);
        }
        );
    }

    public static <T extends OutBoxCM> Specification<T> sortByCreateDate() {
        return ((root, query, builder) ->
        {
            assert query != null;
            query.orderBy(builder.asc(root.get("createdDate")));
            return null;
        }
        );
    }
}
