package project.commondto.dto.user;

import java.util.List;

public interface RolesDefault {
    List<GroupRoles> rolesUser = List.of(
            GroupRoles.USER_AUTH,
            GroupRoles.USER_BOOKING,
            GroupRoles.USER_PAYMENT
    );

    List<GroupRoles> rolesOwner = List.of(
            GroupRoles.OWNER_AUTH,
            GroupRoles.OWNER_BOOKING,
            GroupRoles.OWNER_HOTEL,
            GroupRoles.OWNER_USER,
            GroupRoles.OWNER_ROOM_TYPE,
            GroupRoles.OWNER_PAYMENT
    );

    List<GroupRoles> rolesAdmin = List.of(
            GroupRoles.ADMIN_AUTH,
            GroupRoles.ADMIN_BOOKING,
            GroupRoles.ADMIN_HOTEL,
            GroupRoles.ADMIN_USER,
            GroupRoles.ADMIN_PAYMENT,
            GroupRoles.ADMIN_ROOM_TYPE
    );
}
