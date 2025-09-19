package com.example.bootcamp.infraestructure.adapters;

import com.example.bootcamp.domain.model.Bootcamp;
import com.example.bootcamp.domain.spi.IBootcampPersistencePort;
import com.example.bootcamp.infraestructure.adapters.entity.BootcampCapacityEntity;
import com.example.bootcamp.infraestructure.adapters.entity.BootcampEntity;
import com.example.bootcamp.infraestructure.adapters.mapper.IBootcampEntityMapper;
import com.example.bootcamp.infraestructure.adapters.repository.IBootcampCapacityRepository;
import com.example.bootcamp.infraestructure.adapters.repository.IBootcampRepository;
import com.example.bootcamp.infraestructure.entrypoints.dto.CapacityDTO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Slf4j
@AllArgsConstructor
public class BootcampPersistenceAdapter implements IBootcampPersistencePort {

    private final WebClient webClient;
    private final IBootcampRepository bootcampRepository;
    private final IBootcampEntityMapper bootcampEntityMapper;
    private final IBootcampCapacityRepository bootcampCapacityRepository;

    @Override
    public Mono<Bootcamp> saveBootcamp(Bootcamp bootcamp) {
        BootcampEntity entity = bootcampEntityMapper.toEntity(bootcamp);

        return bootcampRepository.insertBootcamp(
                        entity.getId(),
                        entity.getName(),
                        entity.getDescription(),
                        entity.getLaunchDate(),
                        entity.getDurationInDays()
                )
                .thenReturn(bootcamp);
    }

    @Override
    public Mono<Void> saveBootcampCapacity(UUID bootcampId, List<UUID> capacities) {
        log.info("Saving capacities for bootcamp {}: {}", bootcampId, capacities);

        return Flux.fromIterable(capacities)
                .flatMap(capacityId -> bootcampCapacityRepository.saveBootcampCapacity(
                        UUID.randomUUID(),
                        bootcampId,
                        capacityId
                ))
                .then();
    }

    @Override
    public Mono<Boolean> existByName(String name) {
        return bootcampRepository.findByName(name)
                .map(bootcampEntityMapper::toModel)
                .map(bootcamp -> true)
                .defaultIfEmpty(false);
    }

    @Override
    public Mono<Boolean> validateExistingCapacity(List<UUID> capacities) {
        return webClient.get()
                .uri("/capabilities/simple")
                .retrieve()
                .bodyToFlux(CapacityDTO.class)
                .map(CapacityDTO::getId)
                .collectList()
                .map(allIds -> {
                    log.info("Capacities from capabilities-ms: {}", allIds);
                    return allIds.containsAll(capacities);
                });
    }

    public Mono<Bootcamp> saveBootcampWithCapacities(Bootcamp bootcamp, List<UUID> capacities) {
        return bootcampRepository.save(bootcampEntityMapper.toEntity(bootcamp))
                .map(bootcampEntityMapper::toModel)
                .flatMap(savedBootcamp ->
                        saveBootcampCapacity(savedBootcamp.id(), capacities)
                                .thenReturn(savedBootcamp)
                );
    }
}
