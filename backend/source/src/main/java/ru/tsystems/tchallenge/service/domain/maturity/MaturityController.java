package ru.tsystems.tchallenge.service.domain.maturity;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/maturities/")
@Api(tags = "Maturity")
@SwaggerDefinition(tags = @Tag(name = "Maturity", description = "Retrieve available maturities"))
public class MaturityController {
    @GetMapping
    @ApiOperation("Retrieve all maturities")
    public Maturity[] getAll() {
        return Maturity.values();
    }
}
