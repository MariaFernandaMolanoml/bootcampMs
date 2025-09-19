package com.example.bootcamp.infraestructure.adapters.repository;

import com.example.bootcamp.infraestructure.adapters.entity.BootcampEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface IBootcampRepository extends ReactiveCrudRepository<BootcampEntity, UUID> {
    Mono<BootcampEntity> findByName(String name);
    Mono<Boolean> existsByName(String name);
}
