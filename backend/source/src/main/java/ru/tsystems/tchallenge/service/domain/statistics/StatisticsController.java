package ru.tsystems.tchallenge.service.domain.statistics;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/statistics/")
@PreAuthorize("hasAnyAuthority('REVIEWER', 'ROBOT')")
@Api(tags = "Event statistics")
public class StatisticsController {

    private final StatisticsFacade statisticsFacade;

    @Autowired
    public StatisticsController(StatisticsFacade statisticsFacade) {
        this.statisticsFacade = statisticsFacade;
    }


    @PostMapping
    @ApiOperation(value = "Retrieve statistics for specified event",
            notes = "Event statistics is a list of participants with their workbooks score, sorted by scores")
    public StatisticsResult statisticsResult(@RequestBody StatisticsInvoice invoice) {
        return statisticsFacade.eventStatistics(invoice);
    }

    @PostMapping("/prize-applicants/number")
    public Integer prizeApplicantsNumber(@RequestBody PrizeDrawingInvoice invoice) {
        return statisticsFacade.prizeApplicantsNumber(invoice);
    }

    @PostMapping("/prize-winner")
    public PrizeWinner prizeWinner(@RequestBody PrizeDrawingInvoice invoice) {
        return statisticsFacade.prizeWinner(invoice);
    }


}
