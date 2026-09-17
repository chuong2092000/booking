package project.hotelservice.mapper;

import org.mapstruct.Mapper;

import project.commonutils.mapper.OutboxMapperCM;
import project.hotelservice.entity.Outbox;
import project.hotelservice.utils.JsonHelper;

@Mapper(componentModel = "spring",uses = {JsonHelper.class})
public interface OutboxMapper extends OutboxMapperCM<Outbox> {
}
