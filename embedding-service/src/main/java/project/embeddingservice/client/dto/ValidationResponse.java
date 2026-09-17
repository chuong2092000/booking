package project.embeddingservice.client.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import java.util.Map;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ValidationResponse extends BaseResponse {
    private Map<String, String> errors;
}