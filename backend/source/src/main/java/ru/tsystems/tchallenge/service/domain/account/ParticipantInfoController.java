package ru.tsystems.tchallenge.service.domain.account;

import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.tsystems.tchallenge.service.utility.search.SearchResult;

@RestController
@PreAuthorize("hasAuthority('MODERATOR')")
public class ParticipantInfoController {
    private final ParticipantInfoManager participantInfoManager;

    @Autowired
    public ParticipantInfoController(ParticipantInfoManager participantInfoManager) {
        this.participantInfoManager = participantInfoManager;
    }

    @GetMapping("participant-emails")
    @ApiOperation("Retrieve participant emails")
    @PreAuthorize("hasAuthority('MODERATOR')")
    public SearchResult<String> getParticipantEmails(@RequestParam Integer pageIndex,
                                                     @RequestParam Integer pageSize,
                                                     @RequestParam(defaultValue = "") String filter) {
        return participantInfoManager.findParticipantEmails(pageIndex, pageSize, filter);
    }
}
