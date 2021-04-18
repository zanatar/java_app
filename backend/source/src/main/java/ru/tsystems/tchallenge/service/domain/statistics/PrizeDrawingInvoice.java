package ru.tsystems.tchallenge.service.domain.statistics;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class PrizeDrawingInvoice {
    private String eventId;
    private BigDecimal threshold;
    @ApiModelProperty("List of ownerId (account id), that shouldn't be returned by prize drawing")
    private List<String> excludingOwnerIds;
    private String method;
}
