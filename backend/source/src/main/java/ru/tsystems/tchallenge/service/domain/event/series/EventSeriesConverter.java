package ru.tsystems.tchallenge.service.domain.event.series;

import org.mapstruct.Mapper;
import org.springframework.stereotype.Service;

@Mapper(componentModel = "spring")
@Service
public interface EventSeriesConverter {
    EventSeries toDto(EventSeriesDocument eventSeriesDocument);

}
