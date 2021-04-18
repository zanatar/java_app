package ru.tsystems.tchallenge.service.domain.event;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;
import ru.tsystems.tchallenge.service.domain.maturity.Maturity;
import ru.tsystems.tchallenge.service.utility.data.AbstractDocument;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Document(collection = "events")
@EqualsAndHashCode(callSuper = true)
@Builder
@Data
public final class EventDocument extends AbstractDocument {

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
