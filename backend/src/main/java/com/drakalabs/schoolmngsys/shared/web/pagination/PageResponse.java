package com.drakalabs.schoolmngsys.shared.web.pagination;

import java.util.List;
import org.springframework.data.domain.Page;

/** The pagination envelope every collection endpoint returns (docs/10 §1). */
public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {

    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(), page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }
}
