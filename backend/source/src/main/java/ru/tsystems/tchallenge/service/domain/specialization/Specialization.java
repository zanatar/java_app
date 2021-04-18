package ru.tsystems.tchallenge.service.domain.specialization;

import lombok.Data;
import lombok.NoArgsConstructor;
import ru.tsystems.tchallenge.service.domain.problem.ProblemCategory;

import java.util.List;

@Data
@NoArgsConstructor
public final class Specialization {
    private String id;
    private String caption;
    private String permalink;
    private List<ProblemCategory> problemCategories;
}
