package com.example.bootcamp.infraestructure.adapters;

import com.example.bootcamp.domain.model.Bootcamp;
import com.example.bootcamp.domain.model.BootcampWithCapability;
import com.example.bootcamp.domain.model.Capability;
import com.example.bootcamp.domain.model.Technology;
import com.example.bootcamp.domain.spi.IBootcampPersistencePort;
import com.example.bootcamp.infraestructure.adapters.entity.BootcampCapacityEntity;
import com.example.bootcamp.infraestructure.adapters.entity.BootcampEntity;
import com.example.bootcamp.infraestructure.adapters.mapper.IBootcampEntityMapper;
import com.example.bootcamp.infraestructure.adapters.repository.IBootcampCapacityRepository;
import com.example.bootcamp.infraestructure.adapters.repository.IBootcampRepository;
import com.example.bootcamp.infraestructure.entrypoints.dto.CapacityDTO;
import com.example.bootcamp.infraestructure.entrypoints.util.Constants;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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
                .map(b -> true)
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
                    Set<UUID> allIdsSet = Set.copyOf(allIds);
                    return allIdsSet.containsAll(capacities);
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

    @Override
    public Flux<BootcampWithCapability> getAllBootcamps(String order, String sortBy, int page, int size) {
        int offset = page * size;

        Flux<BootcampEntity> bootcamps = switch (sortBy) {
            case Constants.SORT_BY_NAME -> Constants.ORDER_ASC.equals(order)
                    ? bootcampRepository.findAllByNameAsc(size, offset)
                    : bootcampRepository.findAllByNameDesc(size, offset);
            case Constants.SORT_BY_CAPABILITY_COUNT -> Constants.ORDER_ASC.equals(order)
                    ? bootcampRepository.findAllByCapabilityCountAsc(size, offset)
                    : bootcampRepository.findAllByCapabilityCountDesc(size, offset);
            default -> Flux.error(new IllegalArgumentException("Invalid sortBy parameter"));
        };

        Mono<List<CapacityDTO>> allCapabilitiesMono = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/capabilities")
                        .queryParam("sortBy", "techCount")
                        .queryParam("order", "asc")
                        .queryParam("page", 0)
                        .queryParam("size", 1000)
                        .build())
                .retrieve()
                .bodyToFlux(CapacityDTO.class)
                .collectList();

        return bootcamps
                .flatMap(entity ->
                        bootcampCapacityRepository.findByBootcampId(entity.getId())
                                .collectList()
                                .zipWith(allCapabilitiesMono)
                                .map(tuple -> {
                                    List<BootcampCapacityEntity> bootcampCapacities = tuple.getT1();
                                    List<CapacityDTO> allCapabilities = tuple.getT2();

                                    Set<UUID> capacityIdsSet = bootcampCapacities.stream()
                                            .map(BootcampCapacityEntity::getCapacityId)
                                            .collect(Collectors.toSet());

                                    List<Capability> capabilities = allCapabilities.stream()
                                            .filter(dto -> capacityIdsSet.contains(dto.getId()))
                                            .map(dto -> new Capability(
                                                    dto.getId(),
                                                    dto.getName(),
                                                    dto.getTechnologies() != null
                                                            ? dto.getTechnologies().stream()
                                                            .map(t -> new Technology(t.getId(), t.getName()))
                                                            .sorted(Comparator.comparing(Technology::name))
                                                            .toList()
                                                            : List.of()
                                            ))
                                            .sorted(Comparator.comparing(Capability::name))
                                            .toList();

                                    return new BootcampWithCapability(
                                            entity.getId(),
                                            entity.getName(),
                                            entity.getDescription(),
                                            entity.getLaunchDate(),
                                            entity.getDurationInDays(),
                                            capabilities
                                    );
                                })
                )
                .collectSortedList((a, b) -> {
                    if (Constants.SORT_BY_NAME.equals(sortBy)) {
                        return Constants.ORDER_ASC.equals(order)
                                ? a.name().compareToIgnoreCase(b.name())
                                : b.name().compareToIgnoreCase(a.name());
                    } else {
                        return Constants.ORDER_ASC.equals(order)
                                ? Integer.compare(a.capabilities().size(), b.capabilities().size())
                                : Integer.compare(b.capabilities().size(), a.capabilities().size());
                    }
                })
                .flatMapMany(Flux::fromIterable);
    }
}
