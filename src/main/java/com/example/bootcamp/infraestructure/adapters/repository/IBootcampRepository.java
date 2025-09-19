package com.example.bootcamp.infraestructure.adapters.repository;

import com.example.bootcamp.infraestructure.adapters.entity.BootcampEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.UUID;

public interface IBootcampRepository extends ReactiveCrudRepository<BootcampEntity, UUID> {
    Mono<BootcampEntity> findByName(String name);
    Mono<Boolean> existsByName(String name);
    @Query("INSERT INTO bootcamps (id, name, description, launch_date, duration_in_days) " +
            "VALUES (:id, :name, :description, :launchDate, :durationInDays)")
    Mono<Void> insertBootcamp(@Param("id") UUID id,
                              @Param("name") String name,
                              @Param("description") String description,
                              @Param("launchDate") LocalDate launchDate,
                              @Param("durationInDays") int durationInDays);
}
