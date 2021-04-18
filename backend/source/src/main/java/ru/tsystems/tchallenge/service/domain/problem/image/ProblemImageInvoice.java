package ru.tsystems.tchallenge.service.domain.problem.image;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class ProblemImageInvoice {
    private List<Integer> indices;
    private List<String> names;
    private List<MultipartFile> files;
}
