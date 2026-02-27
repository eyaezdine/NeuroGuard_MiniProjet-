package com.devx.msbienetre.controller;

import com.devx.msbienetre.entity.Wellbeing;
import com.devx.msbienetre.service.WellbeingService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wellbeing")

public class WellbeingController {

    private final WellbeingService service;

    public WellbeingController(WellbeingService service) {
        this.service = service;
    }

    @PostMapping
    public Wellbeing create(@RequestBody Wellbeing wellbeing) {
        return service.createWellbeing(wellbeing);
    }

    @GetMapping
    public List<Wellbeing> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public Wellbeing getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @GetMapping("/user/{userId}")
    public List<Wellbeing> getByUser(@PathVariable String userId) {
        return service.getByUser(userId);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @PutMapping("/{id}")
    public Wellbeing update(@PathVariable Long id, @RequestBody Wellbeing wellbeing) {
        return service.updateWellbeing(id, wellbeing);
    }

}
