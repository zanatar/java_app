package ru.tsystems.tchallenge.service.domain.tag;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.springframework.data.mongodb.core.mapping.Document;
import ru.tsystems.tchallenge.service.utility.data.AbstractDocument;

@Value
@EqualsAndHashCode(callSuper = true)
@Builder
@Document(collection = "tags")
public final class TagDocument extends AbstractDocument {
    String caption;
}
