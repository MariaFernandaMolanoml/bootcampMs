package com.example.bootcamp.infraestructure.adapters.repository;

import com.example.bootcamp.infraestructure.adapters.entity.BootcampCapacityEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface IBootcampCapacityRepository extends ReactiveCrudRepository<BootcampCapacityEntity, UUID> {

    @Query("INSERT INTO bootcamp_capabilities (id, bootcamp_id, capability_id) VALUES (:id, :bootcampId, :capacityId)")
    Mono<Void> saveBootcampCapacity(@Param("id") UUID id,
                                    @Param("bootcampId") UUID bootcampId,
                                    @Param("capacityId") UUID capacityId);

}
