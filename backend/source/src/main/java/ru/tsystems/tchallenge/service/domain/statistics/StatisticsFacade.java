package ru.tsystems.tchallenge.service.domain.statistics;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StatisticsFacade {

    private final StatisticsManager statisticsManager;
    private final PrizeDrawingManager prizeDrawingManager;

    @Autowired
    public StatisticsFacade(StatisticsManager statisticsManager, PrizeDrawingManager prizeDrawingManager) {
        this.statisticsManager = statisticsManager;
        this.prizeDrawingManager = prizeDrawingManager;
    }

    public StatisticsResult eventStatistics(StatisticsInvoice invoice) {
        return statisticsManager.retrieveStatisticsForEvent(invoice);
    }

    public PrizeWinner prizeWinner(PrizeDrawingInvoice invoice) {
        return prizeDrawingManager.prizeWinner(invoice);
    }

    public Integer prizeApplicantsNumber(PrizeDrawingInvoice invoice) {
        return prizeDrawingManager.numberOfApplicants(invoice);
    }

}
