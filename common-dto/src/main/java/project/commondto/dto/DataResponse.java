package project.commondto.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class DataResponse<T> extends BaseResponse {
    private T data;
}
