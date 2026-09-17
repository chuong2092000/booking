package project.hotelservice.mapper;

import org.mapstruct.Mapper;

import project.commonutils.mapper.ProcessEventMapperCM;
import project.hotelservice.entity.ProcessedEvent;
import project.hotelservice.utils.JsonHelper;

@Mapper(componentModel = "spring",uses = {JsonHelper.class})
public interface ProcessEventMapper extends ProcessEventMapperCM<ProcessedEvent> {
}
