package ru.tsystems.tchallenge.service.utility.data;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public final class IdContainer implements IdAware {

    private final String id;
}
