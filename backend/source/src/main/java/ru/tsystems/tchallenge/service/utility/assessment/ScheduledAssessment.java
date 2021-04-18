package ru.tsystems.tchallenge.service.utility.assessment;

import org.springframework.beans.factory.annotation.Value;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.stereotype.Component;
import ru.tsystems.tchallenge.service.domain.workbook.*;
import java.util.stream.*;

@Log4j2
@Component
public class ScheduledAssessment {

    private final WorkbookRepository workbookRepository;
    private final WorkbookManager workbookManager;
    private final WorkbookCodeManager workbookCodeManager;

    @Value("${schedule.delay-time}")
    private String delayTime;

    public ScheduledAssessment( WorkbookRepository workbookRepository, WorkbookManager workbookManager, WorkbookCodeManager workbookCodeManager) {
        this.workbookRepository = workbookRepository;
        this.workbookManager = workbookManager;
        this.workbookCodeManager = workbookCodeManager;
    }

    @Scheduled(fixedDelayString = "${schedule.delay-time}")
    public void assessSubmitted() {
        workbookRepository.findAll()
                .stream()
                .filter( w -> (w.getStatus() == WorkbookStatus.SUBMITTED) )
                .collect(Collectors.toList())
                .forEach( w -> {
                    try{
                        this.workbookCodeManager.assessAllCodeAssignments( w );
                        this.workbookManager.assessSubmittedWorkbook ( w );
                    }
                    catch(ResourceAccessException e){
                        log.info("Codemaster is down! Will retry to asses code solutions in " + this.delayTime + " milliseconds...");
                    }
                } );
    }
}
