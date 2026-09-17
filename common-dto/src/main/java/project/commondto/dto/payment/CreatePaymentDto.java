package project.commondto.dto.payment;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@ToString
public class CreatePaymentDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    @NotEmpty(message = "At least one booking id")
    private List<String> bookingIds;
    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;
    @NotNull(message = "Payment gateway is required")
    private PaymentGateway paymentGateway;
}
