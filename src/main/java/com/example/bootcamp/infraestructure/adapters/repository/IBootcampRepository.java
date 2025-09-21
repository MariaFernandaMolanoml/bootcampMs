package com.example.bootcamp.infraestructure.adapters.repository;

import com.example.bootcamp.infraestructure.adapters.entity.BootcampEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.UUID;

public interface IBootcampRepository extends ReactiveCrudRepository<BootcampEntity, UUID> {
    Mono<BootcampEntity> findByName(String name);
    Mono<Boolean> existsByName(String name);
    @Query("INSERT INTO bootcamps (id, name, description, launch_date, duration_in_days) " +
            "VALUES (:id, :name, :description, :launchDate, :durationInDays)")
    Mono<Void> insertBootcamp(@Param("id") UUID id,
                              @Param("name") String name,
                              @Param("description") String description,
                              @Param("launchDate") LocalDate launchDate,
                              @Param("durationInDays") int durationInDays);


    @Query("SELECT * FROM bootcamps ORDER BY name ASC LIMIT :limit OFFSET :offset")
    Flux<BootcampEntity> findAllByNameAsc(int limit, int offset);

    @Query("SELECT * FROM bootcamps ORDER BY name DESC LIMIT :limit OFFSET :offset")
    Flux<BootcampEntity> findAllByNameDesc(int limit, int offset);

    @Query("""
           SELECT b.* FROM bootcamps b
           JOIN bootcamp_capabilities bc ON b.id = bc.bootcamp_id
           GROUP BY b.id
           ORDER BY COUNT(bc.capability_id) ASC
           LIMIT :limit OFFSET :offset
           """)
    Flux<BootcampEntity> findAllByCapabilityCountAsc(int limit, int offset);

    @Query("""
           SELECT b.* FROM bootcamps b
           JOIN bootcamp_capabilities bc ON b.id = bc.bootcamp_id
           GROUP BY b.id
           ORDER BY COUNT(bc.capability_id) DESC
           LIMIT :limit OFFSET :offset
           """)
    Flux<BootcampEntity> findAllByCapabilityCountDesc(int limit, int offset);
}

