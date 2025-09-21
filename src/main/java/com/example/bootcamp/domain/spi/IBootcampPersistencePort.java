package com.example.bootcamp.domain.spi;

import com.example.bootcamp.domain.model.Bootcamp;
import com.example.bootcamp.domain.model.BootcampWithCapability;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

public interface IBootcampPersistencePort {
    Mono<Bootcamp> saveBootcamp(Bootcamp bootcamp);
    Mono<Void> saveBootcampCapacity(UUID bootcampId, List<UUID> capabilities);
    Mono<Boolean> existByName(String name);
    Mono<Boolean> validateExistingCapacity(List<UUID> capabilities);
    Flux<BootcampWithCapability> getAllBootcamps(String order, String sortBy, int page, int size);
}
