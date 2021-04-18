package ru.tsystems.tchallenge.service.domain.event.series;

import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import ru.tsystems.tchallenge.service.domain.event.EventDocument;
import ru.tsystems.tchallenge.service.domain.event.EventRepository;
import ru.tsystems.tchallenge.service.reliability.exception.OperationException;
import ru.tsystems.tchallenge.service.reliability.exception.OperationExceptionBuilder;
import ru.tsystems.tchallenge.service.utility.search.SearchResult;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static ru.tsystems.tchallenge.service.domain.event.EventManager.eventNotFound;
import static ru.tsystems.tchallenge.service.reliability.exception.OperationExceptionType.ERR_EVENT_IN_ANOTHER_SERIES;
import static ru.tsystems.tchallenge.service.reliability.exception.OperationExceptionType.ERR_INTERNAL;

@Component
@Log4j2
public class EventSeriesManager {
    private final EventSeriesConverter eventSeriesConverter;
    private final EventSeriesRepository eventSeriesRepository;
    private final EventRepository eventRepository;

    public EventSeriesManager(EventSeriesConverter eventSeriesConverter, EventSeriesRepository eventSeriesRepository,
                              EventRepository eventRepository) {
        this.eventSeriesConverter = eventSeriesConverter;
        this.eventSeriesRepository = eventSeriesRepository;
        this.eventRepository = eventRepository;
    }

    EventSeries createSeries(EventSeriesInvoice invoice) {
        invoice.validate();
        Set<EventDocument> events = eventRepository.findByIdIn(invoice.getEventIds());
        if (events.size() < invoice.getEventIds().size()) {
            throw eventNotFound(null);
        }
        events.forEach(eventDocument -> {
            if (eventDocument.getSeriesId() != null) {
                throw eventAlreadyInSeries(eventDocument.getId());
            }
        });
        EventSeriesDocument seriesDocument = EventSeriesDocument.builder()
                .caption(invoice.getCaption())
                .description(invoice.getDescription())
                .eventIds(invoice.getEventIds())
                .build();
        eventSeriesRepository.save(seriesDocument);
        events.forEach(eventDocument -> eventDocument.setSeriesId(seriesDocument.getId()));
        eventRepository.saveAll(events);
        log.info("Created event series " + seriesDocument);
        return eventSeriesConverter.toDto(seriesDocument);
    }

    private OperationException eventAlreadyInSeries(String id) {
        return OperationExceptionBuilder.operationException()
                .textcode(ERR_EVENT_IN_ANOTHER_SERIES)
                .description("Event already in series")
                .attachment(id)
                .build();
    }

    public EventSeries update(String id, EventSeriesInvoice invoice) {
        invoice.validate();
        EventSeriesDocument seriesDocument = eventSeriesRepository.findById(id)
                .orElseThrow(() -> eventSeriesNotFound(id));

        Set<EventDocument> newEvents = eventRepository.findByIdIn(invoice.getEventIds());
        if (newEvents.size() < invoice.getEventIds().size()) {
            throw eventNotFound(null);
        }
        Set<EventDocument> oldEvents = eventRepository.findByIdIn(seriesDocument.getEventIds());
        oldEvents.removeAll(newEvents);
        newEvents.forEach(eventDocument -> {
            if ((eventDocument.getSeriesId() != null) && (!eventDocument.getSeriesId().equals(seriesDocument.getId()))) {
                throw eventAlreadyInSeries(eventDocument.getId());
            } else {
                eventDocument.setSeriesId(seriesDocument.getId());
            }
        });
        oldEvents.forEach(eventDocument -> eventDocument.setSeriesId(null));
        seriesDocument.setCaption(invoice.getCaption());
        seriesDocument.setDescription(invoice.getDescription());
        seriesDocument.setEventIds(invoice.getEventIds());
        eventSeriesRepository.save(seriesDocument);
        eventRepository.saveAll(newEvents);
        eventRepository.saveAll(oldEvents);
        log.info("Updated event series " + seriesDocument);
        return eventSeriesConverter.toDto(seriesDocument);
    }


    private OperationException eventSeriesNotFound(String id) {
        return OperationExceptionBuilder.operationException()
                .textcode(ERR_INTERNAL)
                .description("Event series was not found")
                .attachment(id)
                .build();
    }

    EventSeries retrieveSeriesById(String id) {
        final EventSeriesDocument document = eventSeriesRepository.findById(id)
                .orElseThrow(() -> eventSeriesNotFound(id));
        return eventSeriesConverter.toDto(document);
    }

    public List<EventSeries> retrieveAll() {
        return eventSeriesRepository.findAll()
                .stream()
                .map(eventSeriesConverter::toDto)
                .collect(Collectors.toList());
    }

    SearchResult<EventSeries> retrieveByFilter(String filter, Integer pageIndex, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageIndex, pageSize);
        Page<EventSeriesDocument> seriesPage = eventSeriesRepository
                .findByCaptionContainingIgnoreCase(filter, pageable);
        List<EventSeries> series = seriesPage.map(eventSeriesConverter::toDto).getContent();
        return SearchResult.<EventSeries>builder()
                .items(series)
                .total(seriesPage.getTotalElements())
                .build();
    }
}
