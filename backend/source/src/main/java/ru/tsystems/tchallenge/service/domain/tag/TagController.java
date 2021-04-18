package ru.tsystems.tchallenge.service.domain.tag;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tags/")
@PreAuthorize("hasAnyAuthority('REVIEWER', 'MODERATOR')")
@Api(tags = "Tags")
public class TagController {

    private final TagManager tagManager;

    @Autowired
    public TagController(TagManager tagManager) {
        this.tagManager = tagManager;
    }

    @GetMapping
    @ApiOperation("Retrieve all tags")
    public List<Tag> retrieveAll() {
        return tagManager.retrieveAll();
    }

    @PostMapping
    @ApiOperation("Create new tags. Only for moderators")
    @PreAuthorize("hasAuthority('MODERATOR')")
    public List<Tag> create(@RequestBody TagInvoice invoice) {
        return tagManager.create(invoice);
    }
}
