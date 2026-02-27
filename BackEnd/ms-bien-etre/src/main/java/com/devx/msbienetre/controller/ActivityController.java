package com.devx.msbienetre.controller;

import com.devx.msbienetre.entity.Activity;
import com.devx.msbienetre.service.ActivityService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/activities")
public class ActivityController {

    private final ActivityService service;

    public ActivityController(ActivityService service) {
        this.service = service;
    }

    @PostMapping
    public Activity create(@RequestBody Activity activity) {
        return service.createActivity(activity);
    }

    @GetMapping
    public List<Activity> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public Activity getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @GetMapping("/user/{userId}")
    public List<Activity> getByUser(@PathVariable String userId) {
        return service.getByUser(userId);
    }

    @PutMapping("/{id}")
    public Activity update(@PathVariable Long id, @RequestBody Activity activity) {
        return service.updateActivity(id, activity);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
