package ru.tsystems.tchallenge.service.domain.event;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class EventCongratulationMessage {
    @ApiModelProperty("Minimum test score, from which this message will be displayed")
    private Integer threshold;
    @ApiModelProperty("Message, displayed after submitting the test")
    private String message;
}
