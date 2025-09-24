package com.example.bootcamp.domain.api;

import com.example.bootcamp.domain.model.Bootcamp;
import com.example.bootcamp.domain.model.BootcampWithCapability;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

public interface IBootcampServicePort {
    Mono<Bootcamp> registerBootcamp(Bootcamp bootcamp, List<UUID> capabilities);
    Flux<BootcampWithCapability> getAllBootcamps(String order, String sortBy, int page, int size);
    Mono<Void> deleteBootcamp(UUID bootcampId);
    Mono<Bootcamp> getBootcampById(UUID id);
}