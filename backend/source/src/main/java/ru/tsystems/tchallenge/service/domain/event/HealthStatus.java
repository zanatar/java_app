package ru.tsystems.tchallenge.service.domain.event;

import lombok.Data;
import ru.tsystems.tchallenge.service.domain.maturity.Maturity;
import ru.tsystems.tchallenge.service.domain.problem.ProblemDifficulty;

import java.util.EnumMap;
import java.util.Map;

@Data
public class HealthStatus {
    private String specializationPermalink;
    private Maturity maturity;
    private Map<ProblemDifficulty, Integer> missing = new EnumMap<>(ProblemDifficulty.class);
}
