package com.example.bootcamp.domain.usecase;

import com.example.bootcamp.domain.api.IBootcampServicePort;
import com.example.bootcamp.domain.constants.Constants;
import com.example.bootcamp.domain.enums.Message;
import com.example.bootcamp.domain.exceptions.DomainException;
import com.example.bootcamp.domain.model.Bootcamp;
import com.example.bootcamp.domain.model.BootcampWithCapability;
import com.example.bootcamp.domain.spi.IBootcampPersistencePort;
import reactor.core.publisher.Flux;
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
        UUID newBootcampId = UUID.randomUUID();
        Bootcamp bootcampToSave = new Bootcamp(
                newBootcampId,
                bootcamp.name(),
                bootcamp.description(),
                bootcamp.launchDate(),
                bootcamp.durationInDays(),
                bootcamp.capabilities()
        );
        if (capabilities.isEmpty() || capabilities.size() > Constants.MAX_COUNT) {
            return Mono.error(new DomainException(Message.RANGE_CAPACITIES));
        }

        Set<UUID> uniqueCapabilities = new HashSet<>(capabilities);
        if (uniqueCapabilities.size() != capabilities.size()) {
            return Mono.error(new DomainException(Message.DUPLICATE_CAPACITIES));
        }

        return bootcampPersistencePort.existByName(bootcampToSave.name())
                .filter(exists -> !exists)
                .switchIfEmpty(Mono.error(new DomainException(Message.BOOTCAMP_ALREADY_EXISTS)))
                .flatMap(ignored -> bootcampPersistencePort.validateExistingCapacity(capabilities))
                .flatMap(allExist -> {
                    if (!allExist) {
                        return Mono.error(new DomainException(Message.INVALID_CAPACITIES));
                    }
                    return bootcampPersistencePort.saveBootcamp(bootcampToSave)
                            .flatMap(savedBootcamp ->
                                    bootcampPersistencePort.saveBootcampCapacity(savedBootcamp.id(), capabilities)
                                            .thenReturn(savedBootcamp)
                            );
                });
    }
    @Override
    public Flux<BootcampWithCapability> getAllBootcamps(String order, String sortBy, int page, int size) {
        return bootcampPersistencePort.getAllBootcamps(order, sortBy, page, size);
    }

    @Override
    public Mono<Void> deleteBootcamp(UUID bootcampId) {
        return bootcampPersistencePort.deleteBootcamp(bootcampId);
    }
    @Override
    public Mono<Bootcamp> getBootcampById(UUID id) {
        return bootcampPersistencePort.findById(id);
    }
}
