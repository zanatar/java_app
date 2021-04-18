package ru.tsystems.tchallenge.service.domain.problem.image;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public final class ProblemImage {
    private String url;
    private String name;
    private ProblemImageFormat format;
    private Integer height;
    private Integer width;
    private Integer index;
}
