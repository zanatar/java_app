package ru.tsystems.tchallenge.service.domain.statistics;

import org.mockito.internal.util.collections.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.tsystems.tchallenge.service.domain.account.AccountConverter;
import ru.tsystems.tchallenge.service.domain.account.AccountDocument;
import ru.tsystems.tchallenge.service.domain.account.AccountRepository;
import ru.tsystems.tchallenge.service.domain.workbook.WorkbookDocument;
import ru.tsystems.tchallenge.service.domain.workbook.WorkbookRepository;
import ru.tsystems.tchallenge.service.domain.workbook.WorkbookStatus;
import ru.tsystems.tchallenge.service.reliability.exception.OperationException;
import ru.tsystems.tchallenge.service.reliability.exception.OperationExceptionBuilder;

import java.time.Instant;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import static ru.tsystems.tchallenge.service.domain.workbook.WorkbookStatus.ASSESSED;
import static ru.tsystems.tchallenge.service.reliability.exception.OperationExceptionType.ERR_INTERNAL;
import static ru.tsystems.tchallenge.service.reliability.exception.OperationExceptionType.ERR_NO_MORE_WINNERS;

@Service
public class PrizeDrawingManager {

    private final WorkbookRepository workbookRepository;
    private final AccountRepository accountRepository;
    private final AccountConverter accountConverter;
    private final Set<WorkbookStatus> validStatuses = Sets.newSet(ASSESSED);
    private Random random;

    @Autowired
    public PrizeDrawingManager(WorkbookRepository workbookRepository, AccountRepository accountRepository,
                               AccountConverter accountConverter) {
        this.workbookRepository = workbookRepository;
        this.accountRepository = accountRepository;
        this.accountConverter = accountConverter;
        this.random = new Random();
    }


    public Integer numberOfApplicants(PrizeDrawingInvoice invoice) {
        Set<WorkbookDocument> applicants = workbookRepository
                .findPrizeApplicants(invoice.getEventId(), validStatuses, invoice.getThreshold());
        return applicants.stream()
                .collect(Collectors.groupingBy(WorkbookDocument::getOwnerId)).size();
    }

    public PrizeWinner prizeWinner(PrizeDrawingInvoice invoice) {
        // All reviewed workbooks satisfying avgScore threshold, event and status
        Set<WorkbookDocument> allApplicants = workbookRepository
                .findPrizeApplicants(invoice.getEventId(), validStatuses, invoice.getThreshold());

        // Remove workbooks which is already won prize
        List<WorkbookDocument> applicants = allApplicants.stream()
                .filter(w -> !invoice.getExcludingOwnerIds().contains(w.getOwnerId()))
                .collect(Collectors.toList());

        if (applicants.isEmpty()) {
            throw noMoreWinners();
        }

        WorkbookDocument workbook = applicants.get(this.random.nextInt(applicants.size()));

        PrizeWinner p = null;
        if (invoice.getMethod().equals("random")){
            p = buildPrizeWinner(workbook);
        }
        if (invoice.getMethod().equals("fastest")){
            p = buildPrizeWinner(findWinner(applicants));
        }
        if (invoice.getMethod().equals("top3")){
            p = buildPrizeWinner(findWinner(applicants));
        }
        return p;
    }

    private PrizeWinner buildPrizeWinner(WorkbookDocument workbook) {
        AccountDocument account = accountRepository.findById(workbook.getOwnerId())
                .orElseThrow(() -> internal(workbook.getOwnerId()));
        return PrizeWinner.builder()
                .email(account.getEmail())
                .id(account.getId())
                .personality(accountConverter.toPersonality(account.getPersonality()))
                .score(workbook.getAvgScore())
                .build();

    }

    private OperationException internal(String id) {
        return OperationExceptionBuilder.operationException()
                .textcode(ERR_INTERNAL)
                .description("Account with specified id (workbook ownerId) not found")
                .attachment(id)
                .build();
    }


    private OperationException noMoreWinners() {
        return OperationExceptionBuilder.operationException()
                .textcode(ERR_NO_MORE_WINNERS)
                .description("All participants, that satisfy workbook avgScore threshold is already winners")
                .build();
    }

    private WorkbookDocument findWinner(List<WorkbookDocument> applicants){
        return checkBest(applicants);
    }

    private WorkbookDocument findByPlace(List<WorkbookDocument> applicants, int place){
        WorkbookDocument winner = null;
        int i = 1;
        while (i <= place){
            winner = checkBest(applicants);
            applicants.remove(winner);
            i++;
        }
        return winner;
    }

    private WorkbookDocument checkBest(List<WorkbookDocument> applicants){
        WorkbookDocument winner = null;
        long comparer = Instant.now().toEpochMilli();
        for (WorkbookDocument w : applicants){
            long difference = w.getAssessedAt().toEpochMilli() - w.getCreatedAt().toEpochMilli();
            if (difference < comparer){
                comparer = difference;
                winner = w;
            }
        }
        return winner;
    }

}
