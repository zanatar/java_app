package ru.tsystems.tchallenge.service.domain.event;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
@Builder
public final class EventFilter {

    private Boolean activeOnly;
    private Set<EventStatus> statuses;
    private String permalink;
    private String caption;
    private List<String> seriesIds;
}
