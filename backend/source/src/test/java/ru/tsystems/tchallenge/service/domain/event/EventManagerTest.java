package ru.tsystems.tchallenge.service.domain.event;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import ru.tsystems.tchallenge.service.domain.maturity.Maturity;
import ru.tsystems.tchallenge.service.reliability.exception.OperationException;
import ru.tsystems.tchallenge.service.security.authentication.AuthenticationManager;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static ru.tsystems.tchallenge.service.domain.event.EventStatus.MODIFIED;

@RunWith(SpringRunner.class)
public class EventManagerTest {
    @Mock
    private EventRepository eventRepository;
    @Mock
    private EventConverter eventConverter;
    @InjectMocks
    private EventManager eventManager;
    @Mock
    private AuthenticationManager authenticationManager;
    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    private static final String permalink = "joker";
    private static final String EVENT_ID = UUID.randomUUID().toString();
    private EventDocument eventDocument;
    private EventManagementInvoice invoice;
    private static final String ERR_FORBIDDEN_ACCESS_DENIED = "ERR_FORBIDDEN: Access denied";

    @Before
    public void setUp() {
        Instant start = Instant.now();
        Instant end = start.plus(2, ChronoUnit.DAYS);
        invoice = new EventManagementInvoice();
        invoice.setPermalink(permalink);
        invoice.setCaption("Joker");
        invoice.setMaturities(Collections.singletonList(Maturity.SENIOR));
        invoice.setSpecializationPermalinks(Collections.singletonList("javadev"));
        invoice.setValidFrom(start);
        invoice.setValidUntil(end);
        eventDocument = EventDocument.builder()
                .permalink(permalink)
                .caption("some event")
                .maturities(Collections.singletonList(Maturity.SENIOR))
                .specializationPermalinks(Collections.singletonList("javadev"))
                .validFrom(start)
                .validUntil(end)
                .build();
        eventDocument.setId(EVENT_ID);
    }

    @Test
    public void cannotCreateWhenPermalinkTaken() {
        when(eventRepository.findByPermalinkIgnoreCase(permalink)).thenReturn(eventDocument);
        expectedException.expect(OperationException.class);
        expectedException.expectMessage("ERR_PERMALINK_TAKEN: Event with such permalink already exists");
        eventManager.create(invoice);
        verifyNoMoreInteractions(eventRepository);
    }

    @Test
    public void canCreateWhenPermalinkIsFree() {
        when(eventRepository.findByPermalinkIgnoreCase(permalink)).thenReturn(null);
        eventManager.create(invoice);
        verify(eventRepository).insert(any(EventDocument.class));
    }

    @Test
    public void cannotUpdateDeletedEvents() {
        eventDocument.setStatus(EventStatus.DELETED);
        when(eventRepository.findById(EVENT_ID)).thenReturn(Optional.of(eventDocument));
        expectedException.expect(OperationException.class);
        expectedException.expectMessage(ERR_FORBIDDEN_ACCESS_DENIED);
        eventManager.update(EVENT_ID, invoice);
        verifyNoMoreInteractions(eventRepository);
    }

    @Test
    public void updateApprovedEvent() {
        eventDocument.setStatus(EventStatus.APPROVED);
        invoice.setStatus(EventStatus.APPROVED);
        when(eventRepository.findById(EVENT_ID)).thenReturn(Optional.of(eventDocument));
        eventManager.update(EVENT_ID, invoice);
        ArgumentCaptor<EventDocument> captor = ArgumentCaptor.forClass(EventDocument.class);
        verify(eventRepository).save(captor.capture());
        assertEquals(captor.getValue().getStatus(), MODIFIED);
    }

}
