package project.commonutils.mapper;

import project.commondto.dto.ProcessedEventDto;
import project.commonutils.entity.core.ProcessedEventCM;


public interface ProcessEventMapperCM<T extends ProcessedEventCM> {
    T toEntity(ProcessedEventDto processedEventDto);

    ProcessedEventDto toDto(T processedEvent);
}
