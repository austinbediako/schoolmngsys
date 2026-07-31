package com.drakalabs.schoolmngsys.shared.web.pagination;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/** Builds a {@link Pageable} enforcing the size cap from docs/10 §1 ({@code ?page=0&size=20}, cap 100). */
public final class PageRequestFactory {

    public static final int DEFAULT_SIZE = 20;
    public static final int MAX_SIZE = 100;

    private PageRequestFactory() {
    }

    public static Pageable of(int page, int size) {
        return of(page, size, Sort.unsorted());
    }

    public static Pageable of(int page, int size, Sort sort) {
        int safePage = Math.max(page, 0);
        int safeSize = size <= 0 ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);
        return PageRequest.of(safePage, safeSize, sort);
    }
}
