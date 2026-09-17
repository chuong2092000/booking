package project.paymentservice.mapper;

import org.mapstruct.Mapper;

import project.commonutils.mapper.ProcessEventMapperCM;
import project.paymentservice.entity.ProcessedEvent;
import project.paymentservice.utils.JsonHelper;

@Mapper(componentModel = "spring",uses = {JsonHelper.class})
public interface ProcessEventMapper extends ProcessEventMapperCM<ProcessedEvent> {
}
