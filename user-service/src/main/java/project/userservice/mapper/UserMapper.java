package project.userservice.mapper;

import org.keycloak.representations.idm.UserRepresentation;
import org.mapstruct.Mapper;
import project.commondto.dto.user.UserResponse;
import project.commonutils.config.CentralMapperConfig;
@Mapper(config = CentralMapperConfig.class)
public interface UserMapper{
    UserResponse toRes(UserRepresentation userRepresentation);
}
