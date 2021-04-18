package ru.tsystems.tchallenge.service.domain.event.series;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;
import ru.tsystems.tchallenge.service.utility.data.AbstractDocument;

import java.util.List;

@Document(collection = "series")
@EqualsAndHashCode(callSuper = true)
@Builder
@Data
public class EventSeriesDocument extends AbstractDocument {
    private String caption;
    private String description;
    private List<String> eventIds;
}
