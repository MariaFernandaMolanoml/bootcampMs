package com.example.bootcamp.domain.usecase;

import com.example.bootcamp.domain.api.IBootcampServicePort;
import com.example.bootcamp.domain.constants.Constants;
import com.example.bootcamp.domain.enums.Message;
import com.example.bootcamp.domain.exceptions.DomainException;
import com.example.bootcamp.domain.model.Bootcamp;
import com.example.bootcamp.domain.spi.IBootcampPersistencePort;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class BootcampUseCase implements IBootcampServicePort {

    private final IBootcampPersistencePort bootcampPersistencePort;

    public BootcampUseCase(IBootcampPersistencePort bootcampPersistencePort) {
        this.bootcampPersistencePort = bootcampPersistencePort;
    }

    @Override
    public Mono<Bootcamp> registerBootcamp(Bootcamp bootcamp, List<UUID> capabilities) {

        if (capabilities.isEmpty() || capabilities.size() > Constants.MAX_COUNT) {
            return Mono.error(new DomainException(Message.RANGE_CAPACITIES));
        }

        Set<UUID> uniqueCapabilities = new HashSet<>(capabilities);
        if (uniqueCapabilities.size() != capabilities.size()) {
            return Mono.error(new DomainException(Message.DUPLICATE_CAPACITIES));
        }

        return bootcampPersistencePort.existByName(bootcamp.name())
                .filter(exists -> !exists)
                .switchIfEmpty(Mono.error(new DomainException(Message.BOOTCAMP_ALREADY_EXISTS)))
                .flatMap(ignored -> bootcampPersistencePort.validateExistingCapacity(capabilities))
                .flatMap(allExist -> {
                    if (!allExist) {
                        return Mono.error(new DomainException(Message.INVALID_CAPACITIES));
                    }
                    return bootcampPersistencePort.saveBootcamp(bootcamp)
                            .flatMap(savedBootcamp ->
                                    bootcampPersistencePort.saveBootcampCapacity(savedBootcamp.id(), capabilities)
                                            .thenReturn(savedBootcamp)
                            );
                });
    }
    }
