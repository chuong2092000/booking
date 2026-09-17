package project.commonutils.entity.core;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.MappedSuperclass;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import project.commondto.dto.ProcessedEventStatus;


@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
public class ProcessedEventCM extends CommonEntity {
    @Column(name = "ref_id", nullable = false, unique = true)
    private String refId;

    @Column(name = "value", nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    private JsonNode value;

    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private ProcessedEventStatus status;

    @Column(name = "error_message")
    private String errorMessage;
}
