package ru.tsystems.tchallenge.service.domain.event.series;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.tsystems.tchallenge.service.utility.search.SearchResult;

import java.util.List;

@RestController
@RequestMapping("/events/series/")
@PreAuthorize("hasAnyAuthority('PARTICIPANT', 'REVIEWER', 'MODERATOR')")
@Api(tags = "Event series")
public class EventSeriesController {
    private final EventSeriesManager eventSeriesManager;

    @Autowired
    public EventSeriesController(EventSeriesManager eventSeriesManager) {
        this.eventSeriesManager = eventSeriesManager;
    }

    @GetMapping("{id}")
    @ApiOperation("Return event series with specified id")
    public EventSeries getById(@PathVariable String id) {
        return eventSeriesManager.retrieveSeriesById(id);
    }

    @GetMapping
    @ApiOperation("Return all event series")
    public List<EventSeries> getAll() {
        return eventSeriesManager.retrieveAll();
    }

    @GetMapping("filtered/")
    @ApiOperation("Retrieve event series satisfying given filter")
    @PreAuthorize("hasAnyAuthority('MODERATOR', 'REVIEWER')")
    public SearchResult<EventSeries> get(@RequestParam(defaultValue = "") String filter,
                                         @RequestParam Integer pageIndex,
                                         @RequestParam Integer pageSize) {
        return eventSeriesManager.retrieveByFilter(filter, pageIndex, pageSize);
    }

    @PostMapping
    @ApiOperation("Create a series of events. Requires moderator role")
    @PreAuthorize("hasAuthority('MODERATOR')")
    public EventSeries create(@RequestBody EventSeriesInvoice invoice) {
        return eventSeriesManager.createSeries(invoice);
    }

    @PutMapping("{id}")
    @PreAuthorize("hasAuthority('MODERATOR')")
    @ApiOperation("Update specified event series. Requires moderator role")
    public EventSeries update(@PathVariable
                        @ApiParam("Id of event series to update")
                                String id,
                        @ApiParam("Desired event series data")
                        @RequestBody EventSeriesInvoice invoice) {
        return eventSeriesManager.update(id, invoice);
    }
}
