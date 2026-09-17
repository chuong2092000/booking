package project.paymentservice.mapper;

import org.mapstruct.Mapper;

import project.commonutils.mapper.OutboxMapperCM;
import project.paymentservice.entity.Outbox;
import project.paymentservice.utils.JsonHelper;

@Mapper(componentModel = "spring",uses = {JsonHelper.class})
public interface OutboxMapper extends OutboxMapperCM<Outbox> {
}
