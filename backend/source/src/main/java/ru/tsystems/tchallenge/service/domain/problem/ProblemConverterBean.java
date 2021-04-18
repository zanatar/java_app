package ru.tsystems.tchallenge.service.domain.problem;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import ru.tsystems.tchallenge.service.domain.problem.image.ProblemImage;
import ru.tsystems.tchallenge.service.domain.problem.image.ProblemImageDocument;
import ru.tsystems.tchallenge.service.domain.problem.option.ProblemOption;

@Service
@Primary
@RequiredArgsConstructor
public class ProblemConverterBean implements ProblemConverter {

    private final ProblemConverter converter;

    @Override
    public Problem toDto(ProblemDocument problemDocument) {
        Problem problem = converter.toDto(problemDocument);
        transformOptions(problem, false);
        return problem;
    }

    @Override
    public Problem toDtoClassified(ProblemDocument problemDocument) {
        Problem problem = converter.toDto(problemDocument);
        transformOptions(problem, true);
        return problem;
    }

    @Override
    public ProblemImageDocument fromProblemImage(ProblemImage problemImage) {
        return converter.fromProblemImage(problemImage);
    }

    private void transformOptions(Problem problem, boolean classified) {
        if (problem == null) {
            return;
        }
        int index = 0;
        for (ProblemOption option : problem.getOptions()) {
            index++;
            char textcode = (char) (index + 64);
            option.setIndex(index);
            option.setTextcode(String.valueOf(textcode));
            option.setCorrect(classified ? null : option.getCorrect());
        }
    }
}
