package ru.tsystems.tchallenge.service.domain.event;

import lombok.Data;
import ru.tsystems.tchallenge.service.domain.maturity.Maturity;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Data
public final class Event {
    private String id;
    private String permalink;
    private String caption;
    private String description;
    private String greeting;
    private Instant validFrom;
    private Instant validUntil;
    private List<String> notifications;
    private List<EventCongratulationMessage> congratulations;
    private Integer reviewThreshold;
    private List<Maturity> maturities;
    private List<String> specializationPermalinks;
    private EventStatus status;
    private Integer numberOfAttempts;
    private Set<String> tagIds;
    private Boolean whiteListOnly;
    private List<String> emails;
    private String seriesId;
}
