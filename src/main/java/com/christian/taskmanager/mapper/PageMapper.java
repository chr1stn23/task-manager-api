package com.christian.taskmanager.mapper;

import com.christian.taskmanager.dto.response.PageResponse;
import org.springframework.data.domain.Page;

public class PageMapper {

    private PageMapper(){}

    public static <T> PageResponse<T> from(Page<T> page) {
        return PageResponse.<T>builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .nextPage(page.hasNext() ? page.getNumber() + 1 : null)
                .previousPage(page.hasPrevious() ? page.getNumber() - 1 : null)
                .build();
    }
}
