package project.paymentservice.mapper;

import org.mapstruct.Mapper;
import project.commondto.dto.payment.PaymentDto;
import project.commonutils.config.CentralMapperConfig;
import project.paymentservice.entity.Payment;

@Mapper(config = CentralMapperConfig.class)
public interface PaymentMapper {
    Payment toEntity(PaymentDto paymentDto);
    PaymentDto toDto(Payment payment);
}
