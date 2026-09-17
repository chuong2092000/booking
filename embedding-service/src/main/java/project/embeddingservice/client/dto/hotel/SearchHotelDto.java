package project.embeddingservice.client.dto.hotel;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchHotelDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String id;
    private String keycloakId;
    private String name;
}
