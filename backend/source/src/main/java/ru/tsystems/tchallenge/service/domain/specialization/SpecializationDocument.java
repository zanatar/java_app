package ru.tsystems.tchallenge.service.domain.specialization;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.springframework.data.mongodb.core.mapping.Document;
import ru.tsystems.tchallenge.service.domain.problem.ProblemCategory;
import ru.tsystems.tchallenge.service.utility.data.AbstractDocument;

import java.util.List;

@Value
@EqualsAndHashCode(callSuper = true)
@Builder
@Document(collection = "specializations")
public final class SpecializationDocument extends AbstractDocument {
    String caption;
    String permalink;
    List<ProblemCategory> problemCategories;
}
