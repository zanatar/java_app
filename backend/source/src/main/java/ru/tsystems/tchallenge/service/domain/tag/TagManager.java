package ru.tsystems.tchallenge.service.domain.tag;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@Log4j2
public class TagManager {

    private final TagRepository tagRepository;
    private final TagConverter tagConverter;

    @Autowired
    public TagManager(TagRepository tagRepository,
                      TagConverter tagConverter) {
        this.tagRepository = tagRepository;
        this.tagConverter = tagConverter;
    }

    public List<Tag> retrieveAll() {
        return tagRepository
                .findAll()
                .stream()
                .map(tagConverter::toDto)
                .collect(Collectors.toList());
    }

    public List<Tag> create(TagInvoice invoice) {
        List<TagDocument> tagDocuments = invoice
                .getCaptions()
                .stream()
                .map(caption -> TagDocument.builder().caption(caption).build())
                .collect(Collectors.toList());
        tagRepository.insert(tagDocuments);
        log.info("Created tags: " + tagDocuments);
        return tagDocuments
                .stream()
                .map(tagConverter::toDto)
                .collect(Collectors.toList());
    }
}
