package ru.tsystems.tchallenge.service.domain.event.series;

import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import ru.tsystems.tchallenge.service.domain.event.EventDocument;
import ru.tsystems.tchallenge.service.domain.event.EventRepository;
import ru.tsystems.tchallenge.service.reliability.exception.OperationException;
import ru.tsystems.tchallenge.service.reliability.exception.OperationExceptionType;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
public class EventSeriesManagerTest {
    @Mock
    private EventSeriesConverter converter;
    @Mock
    private EventSeriesRepository eventSeriesRepository;
    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    EventSeriesManager seriesManager;

    private EventSeriesInvoice invoice;
    private static final String eventId = UUID.randomUUID().toString();
    private static final String event2Id = UUID.randomUUID().toString();
    private static final String otherSeriesId = UUID.randomUUID().toString();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    private EventDocument event;
    private EventDocument event2;
    private List<String> eventIds = Arrays.asList(eventId, event2Id);

    @Before
    public void setUp() {
        invoice = new EventSeriesInvoice();
        invoice.setCaption("Series");
        invoice.setDescription("Description");
        invoice.setEventIds(eventIds);
        event = EventDocument.builder()
                .seriesId(null)
                .build();
        event.setId(eventId);
        event2 = EventDocument.builder()
                .seriesId(null)
                .build();
        event2.setId(event2Id);
    }

    @Test
    public void createSeriesNoEvent() {
        when(eventRepository.findByIdIn(eventIds)).thenReturn(Collections.singleton(event2));
        expectedException.expect(OperationException.class);
        expectedException.expectMessage(OperationExceptionType.ERR_INTERNAL.name());
        seriesManager.createSeries(invoice);
        verifyNoMoreInteractions(eventSeriesRepository);
    }

    @Test
    public void createSeriesEventAlreadyInOtherSeries() {
        event2.setSeriesId(otherSeriesId);
        when(eventRepository.findByIdIn(eventIds)).thenReturn(Sets.newHashSet(event, event2));
        expectedException.expect(OperationException.class);
        expectedException.expectMessage(OperationExceptionType.ERR_EVENT_IN_ANOTHER_SERIES.name());
        seriesManager.createSeries(invoice);
        verifyNoMoreInteractions(eventSeriesRepository);
    }

    @Test
    public void createSeries() {
        when(eventRepository.findByIdIn(eventIds)).thenReturn(Sets.newHashSet(event, event2));
        seriesManager.createSeries(invoice);
        ArgumentCaptor<EventSeriesDocument> captor = ArgumentCaptor.forClass(EventSeriesDocument.class);
        verify(eventSeriesRepository).save(captor.capture());
        assertEquals(eventIds, captor.getValue().getEventIds());
    }

    @Test
    public void updateSeriesEventAlreadyInOtherSeries() {
        when(eventRepository.findByIdIn(eventIds)).thenReturn(Sets.newHashSet(event, event2));
        String seriesId = UUID.randomUUID().toString();
        EventSeriesDocument seriesDocument = EventSeriesDocument.builder()
                .caption("Caption")
                .description("test")
                .eventIds(Collections.singletonList(event.getId()))
                .build();
        seriesDocument.setId(seriesId);
        when(eventSeriesRepository.findById(seriesId)).thenReturn(Optional.of(seriesDocument));
        event.setSeriesId(seriesId);
        event2.setSeriesId(otherSeriesId);
        expectedException.expect(OperationException.class);
        expectedException.expectMessage(OperationExceptionType.ERR_EVENT_IN_ANOTHER_SERIES.name());
        seriesManager.update(seriesId, invoice);
        verifyNoMoreInteractions(eventSeriesRepository);
    }

    @Test
    public void updateSeries() {
        String seriesId = UUID.randomUUID().toString();
        event.setSeriesId(seriesId);
        event2.setSeriesId(null);
        String event3Id = UUID.randomUUID().toString();
        EventDocument event3 = EventDocument.builder()
                .seriesId(seriesId)
                .build();
        when(eventRepository.findByIdIn(eventIds)).thenReturn(Sets.newHashSet(event, event2));
        List<String> oldEventIds = Arrays.asList(event.getId(), event3Id);
        when(eventRepository.findByIdIn(oldEventIds)).thenReturn(Sets.newHashSet(event, event3));

        EventSeriesDocument seriesDocument = EventSeriesDocument.builder()
                .caption("Caption")
                .description("test")
                .eventIds(oldEventIds)
                .build();
        seriesDocument.setId(seriesId);
        when(eventSeriesRepository.findById(seriesId)).thenReturn(Optional.of(seriesDocument));
        seriesManager.update(seriesId, invoice);
        ArgumentCaptor<EventSeriesDocument> captor = ArgumentCaptor.forClass(EventSeriesDocument.class);
        verify(eventSeriesRepository).save(captor.capture());
        assertEquals(this.eventIds, captor.getValue().getEventIds());
    }
}
