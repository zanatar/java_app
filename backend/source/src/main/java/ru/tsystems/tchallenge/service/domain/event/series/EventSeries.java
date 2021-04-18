package ru.tsystems.tchallenge.service.domain.event.series;

import lombok.Data;

import java.util.List;

@Data
public class EventSeries {
    private String id;
    private String caption;
    private String description;
    private List<String> eventIds;
}
