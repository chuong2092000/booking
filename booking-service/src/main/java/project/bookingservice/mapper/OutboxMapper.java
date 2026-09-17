package project.bookingservice.mapper;

import org.mapstruct.Mapper;
import project.bookingservice.entity.Outbox;
import project.bookingservice.utils.JsonHelper;
import project.commonutils.mapper.OutboxMapperCM;

@Mapper(componentModel = "spring",uses = {JsonHelper.class})
public interface OutboxMapper extends OutboxMapperCM<Outbox> {
}
