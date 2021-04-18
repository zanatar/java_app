package ru.tsystems.tchallenge.service.domain.problem.image;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Log4j2
public class ProblemImageManager {

    private final Cloudinary cloudinary;

    private final Map options = ObjectUtils.asMap(
            "folder", "problems-images",
            "transformation", new Transformation().crop("fit").width(360).height(300)
    );

    @Autowired
    public ProblemImageManager(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public List<ProblemImage> uploadImages(ProblemImageInvoice invoice) {

        List<ProblemImage> images = invoice.getFiles().stream()
                .parallel()
                .map(this::toTmpFile)
                .filter(Objects::nonNull)
                .map(this::uploadToCloudinary)
                .filter(Objects::nonNull)
                .map(this::toProblemImage)
                .collect(Collectors.toList());

        for (int i = 0; i < images.size(); i++) {
            ProblemImage problemImage = images.get(i);
            problemImage.setIndex(invoice.getIndices().get(i));
            if (invoice.getNames() != null && !invoice.getNames().isEmpty()) {
                problemImage.setName(invoice.getNames().get(i));
            }
        }

        return images;
    }


    private File toTmpFile(MultipartFile f) {
        try {
            File tempFile = Files.createTempFile("problems", f.getOriginalFilename()).toFile();
            f.transferTo(tempFile);
            return tempFile;
        } catch (IOException e) {
            log.error(e);
        }
        return null;
    }

    private Map uploadToCloudinary(File file) {
        try {
            Map result = cloudinary.uploader().upload(file, options);
            Files.delete(file.toPath());
            return result;
        } catch (IOException e) {
            log.error(e);
        }
        return null;
    }

    private ProblemImage toProblemImage(Map result) {
        return ProblemImage.builder()
                .url((String) result.get("url"))
                .height((Integer) result.get("height"))
                .width((Integer) result.get("width"))
                .format(toProblemImageFormat((String) result.get("format")))
                .build();
    }

    private ProblemImageFormat toProblemImageFormat(String format) {
        try {
            return ProblemImageFormat.valueOf(format.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.error(e);
        }
        return null;
    }
}
