package com.example.bootcamp.domain.api;

import com.example.bootcamp.domain.model.Bootcamp;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

public interface IBootcampServicePort {
    Mono<Bootcamp> registerBootcamp(Bootcamp bootcamp, List<UUID> capabilities);
}