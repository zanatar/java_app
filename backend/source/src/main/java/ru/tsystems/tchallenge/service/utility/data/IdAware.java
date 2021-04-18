package ru.tsystems.tchallenge.service.utility.data;

public interface IdAware {

    String getId();

    default IdAware justId() {
        return IdContainer.builder()
                .id(getId())
                .build();
    }
}
