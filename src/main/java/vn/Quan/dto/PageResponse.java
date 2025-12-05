package vn.Quan.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageResponse<T> {
    private List<T> content;
    private int currentPage;
    private int totalPages;
    private long totalElements;
    private int size;
    private boolean hasNext;
    private boolean hasPrevious;
    
    public static <T> PageResponse<T> of(List<T> content, int currentPage, int totalPages, long totalElements, int size) {
        PageResponse<T> response = new PageResponse<>();
        response.setContent(content);
        response.setCurrentPage(currentPage);
        response.setTotalPages(totalPages);
        response.setTotalElements(totalElements);
        response.setSize(size);
        response.setHasNext(currentPage < totalPages - 1);
        response.setHasPrevious(currentPage > 0);
        return response;
    }
}

