package com.example.bootcamp.infraestructure.adapters;

import ch.qos.logback.core.joran.spi.HttpUtil;
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
import com.example.bootcamp.infraestructure.entrypoints.dto.BootcampReportRequest;
import com.example.bootcamp.infraestructure.entrypoints.dto.CapacityDTO;
import com.example.bootcamp.infraestructure.entrypoints.util.Constants;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.HashSet;
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

        // Insertamos bootcamp nuevo
        return bootcampRepository.insertBootcamp(
                        entity.getId(),
                        entity.getName(),
                        entity.getDescription(),
                        entity.getLaunchDate(),
                        entity.getDurationInDays()
                )
                .thenReturn(bootcamp)
                .flatMap(saved -> saveBootcampCapacity(saved.id(), saved.capabilities())
                        .thenReturn(saved)
                )
                // Convertimos a BootcampWithCapability solo para enviar a reports
                .flatMap(saved -> convertToBootcampWithCapability(saved)
                        .flatMap(bwc -> sendBootcampToReports(bwc)
                                .onErrorResume(ex -> {
                                    log.error("Error notificando a reports-ms: {}", ex.getMessage());
                                    return Mono.empty();
                                })
                                .thenReturn(saved)
                        )
                );
    }

    private Mono<BootcampWithCapability> convertToBootcampWithCapability(Bootcamp bootcamp) {
        if (bootcamp.capabilities().isEmpty()) {
            return Mono.just(new BootcampWithCapability(
                    bootcamp.id(),
                    bootcamp.name(),
                    bootcamp.description(),
                    bootcamp.launchDate(),
                    bootcamp.durationInDays(),
                    List.of()
            ));
        }

        return webClient.get()
                .uri("http://localhost:8081/capabilities")
                .retrieve()
                .bodyToFlux(CapacityDTO.class)
                .collectList()
                .map(allCaps -> {
                    Set<UUID> capIds = new HashSet<>(bootcamp.capabilities());
                    List<Capability> capabilities = allCaps.stream()
                            .filter(dto -> capIds.contains(dto.getId()))
                            .map(dto -> new Capability(
                                    dto.getId(),
                                    dto.getName(),
                                    dto.getTechnologies() != null
                                            ? dto.getTechnologies().stream()
                                            .map(t -> new Technology(t.getId(), t.getName()))
                                            .toList()
                                            : List.of()
                            ))
                            .toList();
                    return new BootcampWithCapability(
                            bootcamp.id(),
                            bootcamp.name(),
                            bootcamp.description(),
                            bootcamp.launchDate(),
                            bootcamp.durationInDays(),
                            capabilities
                    );
                });
    }

    private Mono<Void> sendBootcampToReports(BootcampWithCapability bootcamp) {
        List<String> capabilityNames = bootcamp.capabilities().stream()
                .map(Capability::name)
                .toList();

        List<String> technologyNames = bootcamp.capabilities().stream()
                .flatMap(cap -> cap.technologies().stream())
                .map(Technology::name)
                .distinct()
                .toList();

        var dto = new BootcampReportRequest(
                bootcamp.name(),
                bootcamp.description(),
                bootcamp.launchDate().toString(),
                bootcamp.durationInDays(),
                capabilityNames,
                technologyNames
        );

        return webClient.post()
                .uri("http://localhost:8084/reports/bootcamps")
                .bodyValue(dto)
                .retrieve()
                .bodyToMono(Void.class);
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
                .uri("http://localhost:8081/capabilities/simple")
                .retrieve()
                .bodyToFlux(CapacityDTO.class)
                .map(CapacityDTO::getId)
                .collectList()
                .map(allIds -> Set.copyOf(allIds).containsAll(capacities));
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
                .uri("http://localhost:8081/capabilities?sortBy=techCount&order=asc&page=0&size=1000")
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
    @Override
    public Mono<List<UUID>> findCapabilitiesByBootcampId(UUID bootcampId) {
        return bootcampCapacityRepository.findByBootcampId(bootcampId)
                .map(BootcampCapacityEntity::getCapacityId)
                .collectList();
    }

    @Override
    public Mono<Void> deleteBootcamp(UUID bootcampId) {
        return findCapabilitiesByBootcampId(bootcampId)
                .flatMapMany(Flux::fromIterable)
                .flatMap(capacityId ->
                        webClient.delete()
                                .uri("http://localhost:8081/capabilities/{id}/bootcamp/{bootcampId}", capacityId, bootcampId)
                                .retrieve()
                                .bodyToMono(Void.class)
                                .onErrorResume(ex -> {
                                    log.error("Error eliminando capacidad {} para bootcamp {}", capacityId, bootcampId, ex);
                                    return Mono.empty();
                                })
                )
                .thenMany(
                        bootcampCapacityRepository.findByBootcampId(bootcampId)
                                .flatMap(rel -> bootcampCapacityRepository.deleteById(rel.getId()))
                )
                .then(
                        bootcampRepository.deleteById(bootcampId)
                );
    }

    @Override
    public Mono<Bootcamp> findById(UUID bootcampId) {
        return bootcampRepository.findById(bootcampId)
                .map(bootcampEntityMapper::toModel)
                .flatMap(bootcamp ->
                        bootcampCapacityRepository.findByBootcampId(bootcampId)
                                .map(BootcampCapacityEntity::getCapacityId)
                                .collectList()
                                .flatMap(capIds -> webClient.get()
                                        .uri("http://localhost:8081/capabilities/simple")
                                        .retrieve()
                                        .bodyToFlux(CapacityDTO.class)
                                        .collectList()
                                        .map(allCaps -> {
                                            Set<UUID> capIdsSet = Set.copyOf(capIds);
                                            List<Capability> capabilities = allCaps.stream()
                                                    .filter(dto -> capIdsSet.contains(dto.getId()))
                                                    .map(dto -> new Capability(
                                                            dto.getId(),
                                                            dto.getName(),
                                                            dto.getTechnologies() != null
                                                                    ? dto.getTechnologies().stream()
                                                                    .map(t -> new Technology(t.getId(), t.getName()))
                                                                    .toList()
                                                                    : List.of()
                                                    ))
                                                    .toList();

                                            return new Bootcamp(
                                                    bootcamp.id(),
                                                    bootcamp.name(),
                                                    bootcamp.description(),
                                                    bootcamp.launchDate(),
                                                    bootcamp.durationInDays(),
                                                    capabilities.stream().map(Capability::id).toList()
                                            );
                                        })
                                )
                );
    }
}
