package ru.tsystems.tchallenge.service.domain.event;

import lombok.Data;
import ru.tsystems.tchallenge.service.domain.maturity.Maturity;

import java.util.List;
import java.util.Set;


@Data
public class EventHealthInvoice {
    private List<Maturity> maturities;
    private List<String> specializationPermalinks;
    private Set<String> tagIds;
}
