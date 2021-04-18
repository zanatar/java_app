package ru.tsystems.tchallenge.service.domain.event;

import com.google.common.collect.Sets;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.tsystems.tchallenge.service.utility.search.SearchResult;

import java.util.Collections;
import java.util.List;

import static ru.tsystems.tchallenge.service.security.authentication.AuthenticationManager.getAuthentication;

@RestController
@RequestMapping("/events/")
@PreAuthorize("hasAnyAuthority('PARTICIPANT', 'REVIEWER', 'MODERATOR', 'ROBOT')")
@Api(tags = "Event management")
public class EventController {
    private final EventManager eventManager;

    @Autowired
    public EventController(EventManager eventManager) {
        this.eventManager = eventManager;
    }


    @GetMapping
    @PreAuthorize("hasAnyAuthority('REVIEWER', 'MODERATOR')")
    @ApiOperation("Retrieve all events")
    public SearchResult<Event> retrieveAll() {
        return eventManager.retrieveAll();
    }

    @GetMapping("filtered/")
    @PreAuthorize("hasAnyAuthority('REVIEWER', 'MODERATOR')")
    @ApiOperation("Retrieve events filtered by caption and series")
    public SearchResult<Event> retrieveSearchResult(
            @ApiParam("Caption of event")
            @RequestParam(value = "caption", required = false) String caption,
            @ApiParam("Ids of series")
            @RequestParam(value = "series", required = false) List<String> seriesIds,
            @ApiParam("Size of the page to be returned")
            @RequestParam(value = "pageSize") Integer pageSize,
            @ApiParam("Index of page to be returned")
            @RequestParam(value = "pageIndex") Integer pageIndex,
            @ApiParam("If true return full information about the event")
            @RequestParam(value = "detailed", defaultValue = "true") Boolean detailed) {
        EventFilter filter = EventFilter.builder()
                .activeOnly(false)
                .caption(caption)
                .seriesIds(seriesIds)
                .statuses(Sets.newHashSet(EventStatus.values()))
                .build();
        EventSearchInvoice invoice = EventSearchInvoice.builder()
                .filter(filter)
                .pageSize(pageSize)
                .pageIndex(pageIndex)
                .build();
        return eventManager.retrieveByCaptionAndSeries(invoice, !detailed);

    }

    @GetMapping("active")
    @PreAuthorize("hasAuthority('PARTICIPANT')")
    @ApiOperation("Retrieve active events (validFrom >= now && validUntil < now) " +
            "satisfying a given filter.")
    public SearchResult<Event> retrieveActiveEvents(
            @ApiParam("Regex of permalink of event. (e.g. 'joker*'). If no one specified retrieve all events")
            @RequestParam(value = "permalink", defaultValue = ".*") String permalink,
            @ApiParam("Size of the page to be returned")
            @RequestParam(value = "pageSize", defaultValue = "10") String pageSize,
            @ApiParam("Index of page to be returned")
            @RequestParam(value = "pageIndex", defaultValue = "0") String pageIndex,
            @ApiParam("If true return full information about the event")
            @RequestParam(value = "detailed", defaultValue = "false") Boolean detailed) {
        return eventManager.retrieveSearchResult(searchInvoice(permalink, pageSize, pageIndex,
                Collections.singletonList(EventStatus.APPROVED), true), !detailed);
    }

    @GetMapping("availableForSeries")
    @PreAuthorize("hasAnyAuthority('REVIEWER', 'MODERATOR')")
    @ApiOperation("Retrieve events available for given series")
    public SearchResult<Event> retrieveEventsAvailableForGivenSeries(
            @ApiParam("Series id")
            @RequestParam String seriesId,
            @ApiParam("Caption filter")
            @RequestParam(defaultValue = "") String filter,
            @ApiParam("Size of the page to be returned")
            @RequestParam(value = "pageSize") Integer pageSize,
            @ApiParam("Index of page to be returned")
            @RequestParam(value = "pageIndex") Integer pageIndex) {
        return eventManager.retrieveAvailableForGivenSeries(seriesId, filter, pageIndex, pageSize);
    }

    @GetMapping("{id}")
    @ApiOperation("Return event with specified id")
    public Event getById(@PathVariable String id) {
        return eventManager.retrieveById(id, getAuthentication());
    }

    @GetMapping("bypermalink")
    @ApiOperation("Retrieve event id with specified permalink")
    public Event getByPermalink(
        @ApiParam("Event permalink")
        @RequestParam(value = "permalink") String permalink){
         return eventManager.retrieveByPermalink(permalink, getAuthentication());
    }

    @GetMapping("latest")
    @PreAuthorize("hasAnyAuthority('REVIEWER', 'MODERATOR')")
    @ApiOperation("Return latest event")
    public Event getLatest() {
        return eventManager.retrieveLatest();
    }

    @PostMapping("/health")
    @ApiOperation("Returns information about tasks that are needed to generate workbooks " +
            "for specified specialization and maturity")
    @PreAuthorize("hasAnyAuthority('REVIEWER', 'MODERATOR')")
    public EventHealthStatus getEventHealthStatus(@RequestBody EventHealthInvoice invoice) {
        return eventManager.getEventHealthStatus(invoice);
    }

    @PostMapping
    @ApiOperation("Create new event. Requires moderator role")
    @PreAuthorize("hasAuthority('MODERATOR')")
    public Event create(@RequestBody EventManagementInvoice invoice) {
        return eventManager.create(invoice);
    }

    @PutMapping("{id}")
    @PreAuthorize("hasAuthority('MODERATOR')")
    @ApiOperation("Update specified event. Requires moderator role")
    public Event update(@PathVariable
                        @ApiParam("Id of event to update")
                                String id,
                        @ApiParam("Desired event data")
                        @RequestBody EventManagementInvoice update) {
        return eventManager.update(id, update);
    }

    @GetMapping("statuses")
    @ApiOperation("Returns all event statuses")
    public EventStatus[] getEventStatuses() {
        return EventStatus.values();
    }

    private EventSearchInvoice searchInvoice(String permalink, String pageSize, String pageIndex,
                                             List<EventStatus> statuses, boolean activeOnly) {
        final EventFilter filter = EventFilter.builder()
                .activeOnly(activeOnly)
                .permalink(permalink)
                .statuses(Sets.newHashSet(statuses))
                .build();
        return EventSearchInvoice.builder()
                .filter(filter)
                .pageSize(Integer.parseInt(pageSize))
                .pageIndex(Integer.parseInt(pageIndex))
                .build();
    }

    @GetMapping("permalinkValidator")
    @PreAuthorize("hasAuthority('MODERATOR')")
    @ApiOperation("Check if permalink is free")
    public Boolean isPermalinkFree(@RequestParam("permalink") String permalink) {
        return eventManager.isPermalinkFree(permalink);
    }

}
