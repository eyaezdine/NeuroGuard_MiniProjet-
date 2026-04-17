package com.devx.msbienetre.repository;

import com.devx.msbienetre.entity.Activity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ActivityRepository extends JpaRepository<Activity, Long> {

    List<Activity> findByUserId(String userId);

    List<Activity> findByUserIdAndDateBetween(String userId, LocalDate start, LocalDate end);
}
