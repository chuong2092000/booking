package project.hotelservice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import project.commonutils.entity.core.ProcessedEventCM;

@Entity
@Table(name = "processed_event")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProcessedEvent extends ProcessedEventCM {
}
