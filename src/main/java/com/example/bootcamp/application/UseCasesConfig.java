package com.example.bootcamp.application;

import com.example.bootcamp.domain.api.IBootcampServicePort;
import com.example.bootcamp.domain.spi.IBootcampPersistencePort;
import com.example.bootcamp.domain.usecase.BootcampUseCase;
import com.example.bootcamp.infraestructure.adapters.BootcampPersistenceAdapter;
import com.example.bootcamp.infraestructure.adapters.mapper.IBootcampEntityMapper;
import com.example.bootcamp.infraestructure.adapters.repository.IBootcampCapacityRepository;
import com.example.bootcamp.infraestructure.adapters.repository.IBootcampRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@RequiredArgsConstructor
public class UseCasesConfig {

    private final IBootcampRepository bootcampRepository;
    private final IBootcampCapacityRepository bootcampCapacityRepository;
    private final IBootcampEntityMapper bootcampEntityMapper;
    private final WebClient webClient;

    @Bean
    public IBootcampPersistencePort bootcampPersistencePort() {
        return new BootcampPersistenceAdapter(
                webClient,
                bootcampRepository,
                bootcampEntityMapper,
                bootcampCapacityRepository
        );
    }

    @Bean
    public IBootcampServicePort bootcampServicePort(IBootcampPersistencePort persistencePort) {
        return new BootcampUseCase(persistencePort);
    }
}
