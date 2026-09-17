package project.embeddingservice.client.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PageResponseDto<T> {
    private List<T> content;
    private int totalPages;
    private long totalElements;
    private int number;
    private int size;
}
