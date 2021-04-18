package ru.tsystems.tchallenge.service.domain.problem.image;

import lombok.extern.log4j.Log4j2;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/problems/images/")
@PreAuthorize("hasAuthority('MODERATOR')")
@Log4j2
public class ProblemImageController {

    private final ProblemImageManager problemImageManager;

    public ProblemImageController(ProblemImageManager problemImageManager) {
        this.problemImageManager = problemImageManager;
    }

    @PostMapping
    public List<ProblemImage> uploadImg(@ModelAttribute ProblemImageInvoice invoice) {
        return  problemImageManager.uploadImages(invoice);
    }
}
