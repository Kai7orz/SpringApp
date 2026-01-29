package controller.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PagedResponse<T> {
    private List<T> items;
    private int page;
    private int pageSize;
    private int totalItems;
    private int totalPages;

    public static <T> PagedResponse<T> of(List<T> items, int page, int pageSize, int totalItems) {
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);
        return new PagedResponse<>(items, page, pageSize, totalItems, totalPages);
    }
}
