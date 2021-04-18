package ru.tsystems.tchallenge.service.domain.event;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class EventHealthStatus {
    List<HealthStatus> statuses = new ArrayList<>();

}
