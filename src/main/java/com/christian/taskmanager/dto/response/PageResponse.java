package com.christian.taskmanager.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PageResponse<T> {

    private List<T> content;

    private int page;
    private int size;

    private long totalElements;
    private int totalPages;

    private boolean first;
    private boolean last;

    private boolean hasNext;
    private boolean hasPrevious;

    private Integer nextPage;
    private Integer previousPage;
}
