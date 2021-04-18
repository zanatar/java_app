package ru.tsystems.tchallenge.service.domain.statistics.wallboard;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import ru.tsystems.tchallenge.service.domain.account.AccountPersonality;
import ru.tsystems.tchallenge.service.domain.account.AccountSystemManager;
import ru.tsystems.tchallenge.service.domain.problem.ProblemRandomInvoice;
import ru.tsystems.tchallenge.service.domain.statistics.UserStatistics;
import ru.tsystems.tchallenge.service.domain.workbook.WorkbookDocument;
import ru.tsystems.tchallenge.service.domain.workbook.WorkbookRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;
import static ru.tsystems.tchallenge.service.domain.workbook.WorkbookStatus.APPROVED;

@Service
public class WallboardManager {

    private static final int RATING_LIST_SIZE = 15;
    private final WorkbookRepository workbookRepository;
    private final AccountSystemManager accountManager;
    private final SimpMessagingTemplate template;

    public WallboardManager(WorkbookRepository workbookRepository, AccountSystemManager accountManager,
                            SimpMessagingTemplate template) {
        this.workbookRepository = workbookRepository;
        this.accountManager = accountManager;
        this.template = template;
    }


    private WorkbookDocument bestWorkbook(List<WorkbookDocument> workbooks) {
//        return null;
//        return workbooks.stream()
//                .reduce((acc, w) -> w.getAvgScore().compareTo(acc.getAvgScore()) > 0 ? w : acc)
//                .get();
         Optional<WorkbookDocument> wb = workbooks.stream()
                .reduce((acc, w) -> w.getAvgScore().compareTo(acc.getAvgScore()) > 0 ? w : acc);
         return wb.orElse(null); //Sonarqube considers the previous version to be a bug
    }

    private UserStatistics userStatistics(WorkbookDocument workbookDocument) {
        AccountPersonality owner = accountManager.personalityById(workbookDocument.getOwnerId());
        BigDecimal correctSolved = workbookDocument.getAvgScore();

        return UserStatistics.builder()
                .accountPersonality(owner)
                .correctlySolvedRate(correctSolved)
                .assessedAt(workbookDocument.getAssessedAt())
                .build();
    }



    private void evalRanks(List<UserStatistics> statistics) {
        int minRank = 1;
        int left = 0;

        while (left < statistics.size()) {
            int right = findLastEqualScore(statistics, left);
            int maxRank = minRank + right - left - 1;
            setRanks(statistics, left, right, minRank, maxRank);

            left = right;
            minRank = maxRank + 1;
        }
    }

    // Return last index with equal score exclusive
    private int findLastEqualScore(List<UserStatistics> statistics, int i) {
        BigDecimal score = statistics.get(i).getCorrectlySolvedRate();

        do {
            i++;
        } while (i < statistics.size() &&
                statistics.get(i).getCorrectlySolvedRate().equals(score));
        return i;
    }

    private void setRanks(List<UserStatistics> statistics, int left, int right, int minRank, int maxRank) {
        for (int i = left; i < right; i++) {
            UserStatistics stat = statistics.get(i);
            stat.setMinRank(minRank);
            stat.setMaxRank(maxRank);
        }
    }


    public EventStatistics retrieveWallboardDataForEvent(String eventId) {
        List<WorkbookDocument> workbooks = workbookRepository.findByEventId(eventId);
        Map<String, List<WorkbookDocument>> workbooksByOwner = workbooks
                .stream()
                .collect(Collectors.groupingBy(WorkbookDocument::getOwnerId));
        int participantsCount = workbooksByOwner.size();

        List<WorkbookDocument> workbooksInProgress = workbooks
                .stream()
                .filter(w -> (w.getStatus() == APPROVED) && (!w.getSubmittableUntil().isBefore(Instant.now())))
                .collect(Collectors.toList());
        int workbooksInProgressCount = workbooksInProgress.size();

        List<WorkbookDocument> reviewedWorkbooks = workbooks
                .stream()
                .filter(WorkbookDocument::getReviewed)
                .collect(Collectors.toList());
        Map<String, List<WorkbookDocument>> reviewedWorkbooksByOwner = reviewedWorkbooks
                .stream()
                .collect(Collectors.groupingBy(WorkbookDocument::getOwnerId));
        List<UserStatistics> ratingList = reviewedWorkbooksByOwner.values()
                .stream()
                .map(this::bestWorkbook)
                .map(this::userStatistics)
                .sorted(comparing(UserStatistics::getCorrectlySolvedRate).reversed())
                .limit(RATING_LIST_SIZE)
                .collect(Collectors.toList());
        evalRanks(ratingList);

        Map<Integer, Long> answersStatistics = reviewedWorkbooks
                .stream()
                .collect(Collectors.groupingBy(workbookDocument ->
                                workbookDocument.getAvgScore().multiply(BigDecimal.valueOf(4)).intValue(),
                        Collectors.counting()));

        for (int i = 0; i <= ProblemRandomInvoice.NUMBER; i++) {
           answersStatistics.putIfAbsent(i, 0L);
        }

        return EventStatistics.builder()
                .participants(participantsCount)
                .answersStatistics(answersStatistics)
                .ratingList(ratingList)
                .workbooksInProgress(workbooksInProgressCount)
                .build();
    }

    public void updateStats(String eventId) {
        template.convertAndSend("/wallboard/" + eventId, retrieveWallboardDataForEvent(eventId));
    }
}
