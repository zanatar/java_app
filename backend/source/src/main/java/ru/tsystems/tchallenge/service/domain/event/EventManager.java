package ru.tsystems.tchallenge.service.domain.event;

import com.google.common.collect.ImmutableList;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import ru.tsystems.tchallenge.service.utility.search.SearchResult;
import ru.tsystems.tchallenge.service.domain.account.AccountRole;
import ru.tsystems.tchallenge.service.domain.maturity.Maturity;
import ru.tsystems.tchallenge.service.domain.workbook.WorkbookManager;
import ru.tsystems.tchallenge.service.reliability.exception.OperationException;
import ru.tsystems.tchallenge.service.reliability.exception.OperationExceptionBuilder;
import ru.tsystems.tchallenge.service.security.authentication.UserAuthentication;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static ru.tsystems.tchallenge.service.reliability.exception.OperationExceptionBuilder.forbidden;
import static ru.tsystems.tchallenge.service.reliability.exception.OperationExceptionType.ERR_INTERNAL;
import static ru.tsystems.tchallenge.service.reliability.exception.OperationExceptionType.ERR_PERMALINK_TAKEN;
import static ru.tsystems.tchallenge.service.security.authentication.AuthenticationManager.getAuthentication;


@Component
@Log4j2
public class EventManager {

    private final EventRepository eventRepository;
    private final EventConverter eventConverter;
    private final WorkbookManager workbookManager;

    public EventManager(EventRepository eventRepository, EventConverter eventConverter,
                        WorkbookManager workbookManager) {
        this.eventRepository = eventRepository;
        this.eventConverter = eventConverter;
        this.workbookManager = workbookManager;
    }

    Event retrieveById(String id, UserAuthentication authentication) {
        final EventDocument document = eventRepository.findById(id)
                .orElseThrow(() -> eventNotFound(id));
        if (authentication.getAuthorities().contains(AccountRole.PARTICIPANT)) {
            if (!whiteListed(document)) {
                throw forbidden();
            }
            if (document.getStatus() != EventStatus.APPROVED) {
                return eventConverter.toDtoClassified(document);
            }
        }
        return eventConverter.toDto(document);
    }


    Event retrieveByPermalink(String permalink, UserAuthentication authentication) {
        final EventDocument document = eventRepository.findByPermalinkIgnoreCase(permalink);
        if( document == null ){
            throw eventNotFound(null);
        }
        if (authentication.getAuthorities().contains(AccountRole.PARTICIPANT)) {
            if (!whiteListed(document)) {
                throw forbidden();
            }
            if (document.getStatus() != EventStatus.APPROVED) {
                return eventConverter.toDtoClassified(document);
            }
        }
        return eventConverter.toDto(document);
    }

    SearchResult<Event> retrieveSearchResult(EventSearchInvoice invoice, Boolean shortEvent) {
        List<EventDocument> documents;
        if (invoice.getFilter().getActiveOnly()) {
            documents = retrieveActiveEventsByFilter(invoice);
        } else {
            documents = retrieveAllByFilter(invoice);
        }
        documents = documents.stream()
                .filter(this::whiteListed)
                .collect(Collectors.toList());

        return SearchResult.<Event>builder()
                .items(ImmutableList.copyOf(mapSearchItems(documents, shortEvent)))
                .total(invoice.getFilter().getActiveOnly()
                        ? eventRepository.countActiveEventsByFilter(invoice.getFilter().getStatuses(),
                        invoice.getFilter().getPermalink(),
                        Instant.now())
                        : (long) documents.size())
                .build();
    }

    private boolean whiteListed(EventDocument eventDocument) {
        if ((eventDocument.getWhiteListOnly() != null) && eventDocument.getWhiteListOnly()) {
            UserAuthentication authentication = getAuthentication();
            if (authentication.getAuthorities().contains(AccountRole.PARTICIPANT)) {
                String email = authentication.getAccountEmail();
                if (email == null) {
                    return false;
                }
                return (eventDocument.getEmails() != null) && eventDocument.getEmails()
                        .stream()
                        .anyMatch(email::equalsIgnoreCase);
            }
        }
        return true;

    }

    private List<EventDocument> retrieveAllByFilter(EventSearchInvoice invoice) {
        return eventRepository.findByPermalinkRegexAndStatusIn(invoice.getFilter().getPermalink(),
                invoice.getFilter().getStatuses(), PageRequest.of(invoice.getPageIndex(), invoice.getPageSize()));
    }

    private List<EventDocument> retrieveActiveEventsByFilter(EventSearchInvoice invoice) {
        return eventRepository.findActiveEventsByFilter(invoice.getFilter().getStatuses(), invoice.getFilter().getPermalink(),
                Instant.now(), PageRequest.of(invoice.getPageIndex(), invoice.getPageSize()));

    }

    public SearchResult<Event> retrieveAll() {
        List<EventDocument> eventsDocuments = eventRepository.findAll();
        List<Event> events = mapSearchItems(eventsDocuments, false);
        return SearchResult.<Event>builder()
                .items(events)
                .total((long) events.size())
                .build();
    }

    SearchResult<Event> retrieveAvailableForGivenSeries(String seriesId,
                                                        String filter,
                                                        Integer pageIndex,
                                                        Integer pageSize) {
        Pageable pageable = PageRequest.of(pageIndex, pageSize);
        Page<EventDocument> eventDocuments = eventRepository.findByCaptionContainingIgnoreCaseAndSeriesIdIn(
                filter, Arrays.asList(seriesId, null), pageable);
        List<Event> events = eventDocuments
                .stream()
                .map(eventConverter::toDtoShort)
                .collect(Collectors.toList());
        return SearchResult.<Event>builder()
                .items(events)
                .total(eventDocuments.getTotalElements())
                .build();
    }

    SearchResult<Event> retrieveByCaptionAndSeries(EventSearchInvoice invoice, Boolean shortEvent) {
        invoice.validate();
        String caption = (invoice.getFilter().getCaption() != null) ? invoice.getFilter().getCaption() : "";
        Page<EventDocument> documents;
        Pageable pageable = PageRequest.of(invoice.getPageIndex(), invoice.getPageSize());
        if ((invoice.getFilter().getSeriesIds() != null) && (!invoice.getFilter().getSeriesIds().isEmpty())) {
            documents = eventRepository.findByCaptionContainingIgnoreCaseAndSeriesIdIn(
                    caption,
                    invoice.getFilter().getSeriesIds(),
                    pageable);
        } else {
            documents = eventRepository.findByCaptionContainingIgnoreCase(caption, pageable);
        }
        List<Event> items = documents
                .stream()
                .map(shortEvent ? eventConverter::toDtoShort : eventConverter::toDto)
                .collect(Collectors.toList());
        return SearchResult.<Event>builder()
                .items(ImmutableList.copyOf(items))
                .total(documents.getTotalElements())
                .build();
    }

    public Event create(EventManagementInvoice invoice) {
        invoice.validate();
        if (!isPermalinkFree(invoice.getPermalink())) {
            throw permalinkAlreadyExistsException(invoice.getPermalink());
        }
        EventDocument eventDocument = EventDocument.builder()
                .caption(invoice.getCaption())
                .permalink(invoice.getPermalink())
                .description(invoice.getDescription())
                .greeting(invoice.getGreeting())
                .validFrom(invoice.getValidFrom())
                .validUntil(invoice.getValidUntil())
                .notifications(invoice.getNotifications())
                .congratulations(invoice.getCongratulations())
                .reviewThreshold(invoice.getReviewThreshold())
                .maturities(invoice.getMaturities())
                .specializationPermalinks(invoice.getSpecializationPermalinks())
                .status(EventStatus.CREATED)
                .numberOfAttempts(invoice.getNumberOfAttempts())
                .tagIds(invoice.getTagIds())
                .whiteListOnly(invoice.getWhiteListOnly())
                .emails(invoice.getEmails())
                .seriesId(invoice.getSeriesId())
                .build();

        eventRepository.insert(eventDocument);
        log.info("Created event " + eventDocument);
        return eventConverter.toDto(eventDocument);
    }

    public Event update(String id, EventManagementInvoice invoice) {
        invoice.validate();
        EventDocument eventDocument = eventRepository.findById(id)
                .orElseThrow(() -> eventNotFound(id));

        if ((eventDocument.getStatus() == EventStatus.SUSPENDED) && !onlyStatusChanged(invoice, eventDocument) ||
                (eventDocument.getStatus() == EventStatus.DELETED)){
            throw forbidden();
        }
        if ((eventDocument.getStatus() == EventStatus.APPROVED) && !onlyStatusChanged(invoice, eventDocument)) {
            invoice.setStatus(EventStatus.MODIFIED);
        }
        eventDocument
                .setCaption(invoice.getCaption());
        eventDocument.setDescription(invoice.getDescription());
        eventDocument.setGreeting(invoice.getGreeting());
        eventDocument.setValidFrom(invoice.getValidFrom());
        eventDocument.setValidUntil(invoice.getValidUntil());
        eventDocument.setNotifications(invoice.getNotifications());
        eventDocument.setCongratulations(invoice.getCongratulations());
        eventDocument.setReviewThreshold(invoice.getReviewThreshold());
        eventDocument.setMaturities(invoice.getMaturities());
        eventDocument.setSpecializationPermalinks(invoice.getSpecializationPermalinks());
        eventDocument.setStatus(invoice.getStatus());
        eventDocument.setNumberOfAttempts(invoice.getNumberOfAttempts());
        eventDocument.setTagIds(invoice.getTagIds());
        eventDocument.setWhiteListOnly(invoice.getWhiteListOnly());
        eventDocument.setEmails(invoice.getEmails());
        eventDocument.setSeriesId(invoice.getSeriesId());
        eventRepository.save(eventDocument);
        log.info("Updated event " + eventDocument);
        return eventConverter.toDto(eventDocument);
    }

    private boolean onlyStatusChanged(EventManagementInvoice invoice, EventDocument eventDocument) {
        return invoice.getCaption().equals(eventDocument.getCaption())
                && Objects.equals(invoice.getDescription(), eventDocument.getDescription())
                && Objects.equals(invoice.getGreeting(), eventDocument.getGreeting())
                && invoice.getValidFrom().equals(eventDocument.getValidFrom())
                && invoice.getValidUntil().equals(eventDocument.getValidUntil())
                && invoice.getSpecializationPermalinks().equals(eventDocument.getSpecializationPermalinks())
                && invoice.getMaturities().equals(eventDocument.getMaturities())
                && invoice.getWhiteListOnly().equals(eventDocument.getWhiteListOnly())
                && invoice.getNumberOfAttempts().equals(eventDocument.getNumberOfAttempts())
                && Objects.equals(invoice.getReviewThreshold(), eventDocument.getReviewThreshold())
                && Objects.equals(invoice.getEmails(), eventDocument.getEmails())
                && Objects.equals(invoice.getTagIds(), eventDocument.getTagIds())
                && Objects.equals(invoice.getCongratulations(), eventDocument.getCongratulations())
                && Objects.equals(invoice.getSeriesId(), eventDocument.getSeriesId());
    }

    Boolean isPermalinkFree(String permalink) {
        final EventDocument eventDocument = eventRepository.findByPermalinkIgnoreCase(permalink);
        return eventDocument == null;
    }

    private List<Event> mapSearchItems(final List<EventDocument> documents, Boolean shortEvent) {
        return documents
                .stream()
                .map(shortEvent ? eventConverter::toDtoShort : eventConverter::toDto)
                .collect(Collectors.toList());
    }

    public static OperationException eventNotFound(String id) {
        return OperationExceptionBuilder.operationException()
                .textcode(ERR_INTERNAL)
                .description("Event is not found")
                .attachment(id)
                .build();
    }

    private OperationException permalinkAlreadyExistsException(String permalink) {
        return OperationExceptionBuilder.operationException()
                .textcode(ERR_PERMALINK_TAKEN)
                .description("Event with such permalink already exists")
                .attachment(permalink)
                .build();
    }

    EventHealthStatus getEventHealthStatus(EventHealthInvoice invoice) {
        EventHealthStatus eventHealthStatus = new EventHealthStatus();
        for (Maturity maturity: invoice.getMaturities()) {
            for (String specialization: invoice.getSpecializationPermalinks()) {
                HealthStatus healthStatus = workbookManager.getHealthStatus(
                        maturity, specialization, invoice.getTagIds());
                eventHealthStatus.statuses.add(healthStatus);
            }
        }
        return eventHealthStatus;
    }

    Event retrieveLatest() {
        Pageable pageable = PageRequest.of(0, 1, Sort.by(Sort.Order.desc("validFrom")));
        Iterator<EventDocument> iterator = eventRepository.findAll(pageable).iterator();
        if (iterator.hasNext()) {
            return eventConverter.toDto(iterator.next());
        } else {
            return null;
        }
    }
}
