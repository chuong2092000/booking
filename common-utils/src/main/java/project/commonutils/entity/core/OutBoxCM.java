package project.commonutils.entity.core;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.*;
import lombok.experimental.SuperBuilder;


@Getter
@Setter
@MappedSuperclass
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OutBoxCM extends CommonEntity {

    private String payload;

    private String topic;

    @Builder.Default
    @Column(name = "is_sent")
    private boolean isSent = false;
}