package ru.tsystems.tchallenge.service.utility.search;


import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SearchResult<T> {
    private final List<T> items;
    private final Long total;
}
