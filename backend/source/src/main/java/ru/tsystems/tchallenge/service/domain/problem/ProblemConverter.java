package ru.tsystems.tchallenge.service.domain.problem;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Service;
import ru.tsystems.tchallenge.service.domain.problem.image.ProblemImage;
import ru.tsystems.tchallenge.service.domain.problem.image.ProblemImageDocument;

@Mapper(componentModel = "spring")
@Service
public interface ProblemConverter {

   // @Mapping
    Problem toDto(ProblemDocument problemDocument);

   // @Mapping (target = "problemCodeExpectationItems", ignore = true)
    Problem toDtoClassified(ProblemDocument document);

    ProblemImageDocument fromProblemImage(ProblemImage problemImage);
}
