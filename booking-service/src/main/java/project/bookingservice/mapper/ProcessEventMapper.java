package project.bookingservice.mapper;

import org.mapstruct.Mapper;
import project.bookingservice.entity.ProcessedEvent;
import project.bookingservice.utils.JsonHelper;
import project.commonutils.mapper.ProcessEventMapperCM;

@Mapper(componentModel = "spring",uses = {JsonHelper.class})
public interface ProcessEventMapper extends ProcessEventMapperCM<ProcessedEvent> {
}
