package ru.tsystems.tchallenge.service.domain.workbook;

import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.tsystems.tchallenge.service.domain.problem.Problem;
import ru.tsystems.tchallenge.service.domain.problem.ProblemConverter;
import ru.tsystems.tchallenge.service.domain.problem.ProblemDocument;
import ru.tsystems.tchallenge.service.domain.problem.ProblemRepository;
import ru.tsystems.tchallenge.service.domain.workbook.assignment.Assignment;
import ru.tsystems.tchallenge.service.domain.workbook.assignment.AssignmentDocument;

@Mapper(componentModel = "spring")
@Service
public abstract class WorkbookConverter {

    @Value("${tchallenge.coworker.url}")
    private String coworkerUrl;

    @Autowired
    private ProblemRepository problemRepository;
    @Autowired
    private ProblemConverter problemConverter;

    @Mapping(source = "assignments", target = "assignments", qualifiedByName = "assignmentTransform")
    @Mapping(target = "coworkerLink", ignore = true)
    public abstract Workbook toDto(WorkbookDocument workbookDocument);

    @Mapping(target = "assignments", ignore = true)
    @Mapping(target = "coworkerLink", ignore = true)
    @Mapping(target = "specializationPermalink", ignore = true)
    @Mapping(target = "reviewed", ignore = true)
    @Mapping(target = "maturity", ignore = true)
    @Mapping(target = "textcode", ignore = true)
    public abstract Workbook toDtoShort(WorkbookDocument workbookDocument);

    @Mapping(source = "assignments", target = "assignments", qualifiedByName = "assignmentClassifiedTransform")
    @Mapping(target = "coworkerLink", ignore = true)
    public abstract Workbook toClassifiedDto(WorkbookDocument workbookDocument);

    @Named("assignmentTransform")
    @Mapping(source = "problemId", target = "problem", qualifiedByName = "problemByIdTransform")
    @Mapping(target = "index", ignore = true)
    abstract Assignment toAssignment(AssignmentDocument assignmentDocument);

    @Named("assignmentClassifiedTransform")
    @Mapping(source = "problemId", target = "problem", qualifiedByName = "problemClassifiedByIdTransform")
    @Mapping(target = "index", ignore = true)
    abstract Assignment toAssignmentClassified(AssignmentDocument assignmentDocument);

    @Named("problemByIdTransform")
    Problem problemFromId(String id) {
        ProblemDocument problemDocument = problemRepository.findById(id).orElse(null);
        return problemConverter.toDto(problemDocument);
    }

    @Named("problemClassifiedByIdTransform")
    Problem problemClassifiedFromId(String id) {
        ProblemDocument problemDocument = problemRepository.findById(id).orElse(null);
        return problemConverter.toDtoClassified(problemDocument);
    }


    @AfterMapping
    void setAssignmentIndices(@MappingTarget Workbook workbook) {
        int index = 0;
        if (workbook.getAssignments() != null) {
            for (Assignment assignment : workbook.getAssignments()) {
                index++;
                assignment.setIndex(index);
            }
        }
        workbook.setCoworkerLink(coworkerUrl + "/workbooks/" + workbook.getId() + "/review");
    }
}
