package project.commonutils.mapper;

import project.commondto.dto.OutBoxDto;
import project.commonutils.entity.core.OutBoxCM;


public interface OutboxMapperCM<T extends OutBoxCM> {
    T toEntity(OutBoxDto outBoxDto);

    OutBoxDto toDto(T entity);
}