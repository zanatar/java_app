package ru.tsystems.tchallenge.service.domain.workbook;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import ru.tsystems.tchallenge.service.ServiceApplication;
import ru.tsystems.tchallenge.service.config.EmbeddedMongoConfig;

import java.math.BigDecimal;
import java.util.*;

@DataMongoTest
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {ServiceApplication.class, EmbeddedMongoConfig.class})
public class WorkbookRepositoryTest {

    private UUID eventId;
    private LinkedHashSet<WorkbookDocument> prizeApplicants = new LinkedHashSet<>();

    @Before
    public void init() {
        eventId = UUID.randomUUID();
        ArrayList<WorkbookDocument> documents = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            WorkbookDocument d = genWorkbook(BigDecimal.valueOf(i)
                    .divide(BigDecimal.valueOf(10), 2, BigDecimal.ROUND_HALF_EVEN));
            documents.add(d);
            if (i >= 5) {
                prizeApplicants.add(d);
            }
        }
        for (int i = 0; i < 10; i++) {
            documents.add(genWorkbook(BigDecimal.ZERO, UUID.randomUUID().toString(), WorkbookStatus.APPROVED));
        }
        for (int i = 0; i < 10; i++) {
            documents.add(genWorkbook(BigDecimal.ONE, eventId.toString(), WorkbookStatus.DISCARDED));
        }
        workbookRepository.saveAll(documents);
    }

    @Autowired
    private WorkbookRepository workbookRepository;


    @Test
    public void findPrizeApplicantsTest() {
        Set<WorkbookStatus> statuses = Collections.singleton(WorkbookStatus.APPROVED);
        Set<WorkbookDocument> prizeApplicants = workbookRepository
                .findPrizeApplicants(eventId.toString(), statuses, BigDecimal.valueOf(0.5));
        Assert.assertEquals(new ArrayList<>(this.prizeApplicants), new ArrayList<>(prizeApplicants));

    }

    private WorkbookDocument genWorkbook(BigDecimal avgScore, String eventId, WorkbookStatus status) {
        return WorkbookDocument.builder()
                .eventId(eventId != null ? eventId : this.eventId.toString())
                .avgScore(avgScore)
                .reviewed(true)
                .status(status != null ? status : WorkbookStatus.APPROVED)
                .build();
    }

    private WorkbookDocument genWorkbook(BigDecimal avgScore) {
        return genWorkbook(avgScore, null, null);
    }
}
