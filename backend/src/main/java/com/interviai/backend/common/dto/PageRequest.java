package com.interviai.backend.common.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

/**
 * Standard pagination request DTO for all paginated endpoints.
 * 
 * @author InterviAI Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageRequest {

    @Min(value = 0, message = "Page number must be greater than or equal to 0")
    @Builder.Default
    private int page = 0;

    @Min(value = 1, message = "Page size must be greater than 0")
    @Max(value = 100, message = "Page size must not exceed 100")
    @Builder.Default
    private int size = 20;

    private List<SortOrder> sort;

    /**
     * Sort order specification.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SortOrder {
        private String property;
        
        @Builder.Default
        private Direction direction = Direction.ASC;
    }

    /**
     * Sort direction enum.
     */
    public enum Direction {
        ASC, DESC
    }

    /**
     * Convert to Spring Data Pageable.
     */
    public Pageable toPageable() {
        Sort springSort = Sort.unsorted();
        
        if (sort != null && !sort.isEmpty()) {
            List<Sort.Order> orders = sort.stream()
                    .map(s -> new Sort.Order(
                            s.direction == Direction.ASC ? Sort.Direction.ASC : Sort.Direction.DESC,
                            s.property
                    ))
                    .toList();
            springSort = Sort.by(orders);
        }
        
        return org.springframework.data.domain.PageRequest.of(page, size, springSort);
    }

    /**
     * Create default page request.
     */
    public static PageRequest defaultRequest() {
        return PageRequest.builder()
                .page(0)
                .size(20)
                .build();
    }

    /**
     * Create page request with custom size.
     */
    public static PageRequest of(int page, int size) {
        return PageRequest.builder()
                .page(page)
                .size(size)
                .build();
    }

    /**
     * Create page request with sorting.
     */
    public static PageRequest of(int page, int size, String property, Direction direction) {
        return PageRequest.builder()
                .page(page)
                .size(size)
                .sort(List.of(SortOrder.builder()
                        .property(property)
                        .direction(direction)
                        .build()))
                .build();
    }
}