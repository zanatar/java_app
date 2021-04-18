package ru.tsystems.tchallenge.service.utility.search;

import lombok.Data;

import java.time.Instant;

@Data
public class Filter {
    private String filter;
    private String filterTo;
    private String[] values;
    private Instant dateFrom;
    private Instant dateTo;
    private FilterType filterType;
    private Integer hour;
    private Integer minute;
    private String utcOffset;
    private Type type;
}

enum FilterType {
    text, number, date
}
