package com.devx.msbienetre.repository;

import com.devx.msbienetre.entity.Wellbeing;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface WellbeingRepository extends JpaRepository<Wellbeing, Long> {

    List<Wellbeing> findByUserId(String userId);

    List<Wellbeing> findByUserIdAndDateBetween(String userId, LocalDate start, LocalDate end);

    boolean existsByUserIdAndDate(String userId, LocalDate date);
}
