package project.authservice.exception;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class KeycloakException {
    private String error;

    @JsonProperty("error_description")
    private String errorDescription;
}