package ru.tsystems.tchallenge.service.security.voucher;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/security/vouchers/")
@Api(tags = "Voucher")
public class SecurityVoucherController {

    private final SecurityVoucherFacade securityVoucherFacade;

    public SecurityVoucherController(SecurityVoucherFacade securityVoucherFacade) {
        this.securityVoucherFacade = securityVoucherFacade;
    }

    @PostMapping
    @ApiOperation("Send email letter with voucher (need to set up password and verify account)")
    public SecurityVoucher createVoucher(@RequestBody SecurityVoucherInvoice invoice) {
        return securityVoucherFacade.createAndSend(invoice);
    }

    @GetMapping()
    @ApiOperation(value = "Retrieve voucher by payload. If payload is invalid or voucher is expired return null")
    public SecurityVoucher getByPayload(@RequestParam(name = "payload") String payload) {
        return securityVoucherFacade.getByPayload(payload);
    }
}
