package project.commondto.dto;

import lombok.*;

import java.util.Map;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ValidationResponse extends BaseResponse {
    private Map<String, String> errors;
}