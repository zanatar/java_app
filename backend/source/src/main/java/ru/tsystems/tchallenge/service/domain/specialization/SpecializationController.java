package ru.tsystems.tchallenge.service.domain.specialization;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/specializations/")
@Api(tags = "Specializations")
public class SpecializationController {

    private final SpecializationManager specializationManager;

    @Autowired
    public SpecializationController(SpecializationManager specializationManager) {
        this.specializationManager = specializationManager;
    }

    @GetMapping
    @ApiOperation("Retrieve all specializations")
    public List<Specialization> retrieveAll() {
        return specializationManager.retrieveAll();
    }
}
