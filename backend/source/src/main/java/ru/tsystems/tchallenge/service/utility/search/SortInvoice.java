package ru.tsystems.tchallenge.service.utility.search;

import lombok.Data;

@Data
public class SortInvoice<T> {
    private T key;
    private Boolean ascending;
}
