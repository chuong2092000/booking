package project.paymentservice.entity;


import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import project.commonutils.entity.core.OutBoxCM;


@Entity
@Table(name = "out_box")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Outbox extends OutBoxCM {
}
