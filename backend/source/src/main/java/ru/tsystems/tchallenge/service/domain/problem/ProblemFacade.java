package ru.tsystems.tchallenge.service.domain.problem;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.tsystems.tchallenge.service.utility.search.SearchResult;

import java.util.List;

@Component
public class ProblemFacade {

    private ProblemManager problemManager;

    @Autowired
    public ProblemFacade(ProblemManager problemManager) {
        this.problemManager = problemManager;
    }

    public Problem create(ProblemInvoice invoice) {
        return problemManager.create(invoice);
    }

    public List<Problem> retrieveAll() {
        return problemManager.retrieveAll();
    }

    public Problem update(String id, ProblemInvoice invoice) {
        return problemManager.update(id, invoice);
    }

    List<Problem> retrieveRandom(ProblemRandomInvoice invoice) {
        return problemManager.retrieveRandom(invoice);
    }

    SearchResult<Problem> retrieveByFilter(ProblemSearchInvoice invoice) {
        return problemManager.retrieveByFilter(invoice);
    }
}
