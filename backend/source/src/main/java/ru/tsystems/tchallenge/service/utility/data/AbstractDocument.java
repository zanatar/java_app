package ru.tsystems.tchallenge.service.utility.data;

import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
public abstract class AbstractDocument {
    @Id
    private String id;
}
