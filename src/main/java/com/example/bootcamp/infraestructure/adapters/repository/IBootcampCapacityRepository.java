package com.example.bootcamp.infraestructure.adapters.repository;

import com.example.bootcamp.infraestructure.adapters.entity.BootcampCapacityEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface IBootcampCapacityRepository extends ReactiveCrudRepository<BootcampCapacityEntity, UUID> {

    @Query("INSERT INTO bootcamp_capacity (bootcamp_id, capacity_id) VALUES (:bootcampId, :capacityId)")
    Mono<Void> saveBootcampCapacity(UUID bootcampId, UUID capacityId);
}
