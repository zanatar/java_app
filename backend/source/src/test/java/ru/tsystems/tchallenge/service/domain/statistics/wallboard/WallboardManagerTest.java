package ru.tsystems.tchallenge.service.domain.statistics.wallboard;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import ru.tsystems.tchallenge.service.domain.account.AccountSystemManager;
import ru.tsystems.tchallenge.service.domain.statistics.UserStatistics;
import ru.tsystems.tchallenge.service.domain.workbook.WorkbookDocument;
import ru.tsystems.tchallenge.service.domain.workbook.WorkbookRepository;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class WallboardManagerTest {

    private static final String HEX = "5b409f0c27eaab2ebcb0690e";
    private WallboardManager wallboardManager;
    @Mock
    private WorkbookRepository workbookRepository;
    @Mock
    private AccountSystemManager accountManager;
    @Mock
    private SimpMessagingTemplate template;

    private List<BigDecimal> expected;

    @Before
    public void init() {
        List<WorkbookDocument> workbookDocuments = generateData();
        when(accountManager.personalityById(any())).thenReturn(null);
        when(workbookRepository.findByEventId(any())).thenReturn(workbookDocuments);
        wallboardManager = new WallboardManager(workbookRepository, accountManager, template);
    }

    private List<WorkbookDocument> generateData() {
        String user1Id = UUID.randomUUID().toString();
        String user2Id = UUID.randomUUID().toString();

        expected = Arrays.asList(
                BigDecimal.valueOf(0.95),
                BigDecimal.valueOf(0.85)
        );

        return Arrays.asList(
                createWorkbook(user1Id, 0.25),
                createWorkbook(user1Id, 0.63),
                createWorkbook(user1Id, 0),
                createWorkbook(user1Id, 0.85),
                createWorkbook(user2Id, 0.53),
                createWorkbook(user2Id, 0.95)
        );
    }

    private WorkbookDocument createWorkbook(String ownerId, double score) {
        WorkbookDocument workbook = Mockito.mock(WorkbookDocument.class);
        when(workbook.getOwnerId()).thenReturn(ownerId);
        when(workbook.getReviewed()).thenReturn(true);
        when(workbook.getAvgScore()).thenReturn(BigDecimal.valueOf(score));
        return workbook;
    }



    @Test
    public void testWallboardData() {
        EventStatistics statistics = wallboardManager.retrieveWallboardDataForEvent(HEX);
        List<BigDecimal> actual = statistics
                .getRatingList()
                .stream()
                .map(UserStatistics::getCorrectlySolvedRate)
                .collect(Collectors.toList());
        Assert.assertEquals(expected, actual);
    }

}
