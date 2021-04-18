package ru.tsystems.tchallenge.service.domain.problem.image;


import lombok.Data;

@Data
public final class ProblemImageDocument {
    String url;
    private String name;
    private ProblemImageFormat format;
    private Integer height;
    private Integer width;
}
