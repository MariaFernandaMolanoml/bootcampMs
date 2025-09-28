package com.example.bootcamp;

import com.example.bootcamp.domain.enums.Message;
import com.example.bootcamp.domain.exceptions.DomainException;
import com.example.bootcamp.domain.model.Bootcamp;
import com.example.bootcamp.domain.model.BootcampWithCapability;
import com.example.bootcamp.domain.spi.IBootcampPersistencePort;
import com.example.bootcamp.domain.usecase.BootcampUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BootcampUseCaseTest {

    @Mock
    private IBootcampPersistencePort bootcampPersistencePort;

    @InjectMocks
    private BootcampUseCase bootcampUseCase;

    private Bootcamp bootcamp;
    private UUID capabilityId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        capabilityId = UUID.randomUUID();
        bootcamp = new Bootcamp(
                UUID.randomUUID(),
                "Bootcamp Dev",
                "Descripcion",
                LocalDate.now(),
                60,
                null
        );
    }

    @Test
    void registerBootcamp_success() {
        when(bootcampPersistencePort.existByName(any())).thenReturn(Mono.just(false));
        when(bootcampPersistencePort.validateExistingCapacity(any())).thenReturn(Mono.just(true));
        when(bootcampPersistencePort.saveBootcamp(any())).thenReturn(Mono.just(bootcamp));
        when(bootcampPersistencePort.saveBootcampCapacity(any(), any())).thenReturn(Mono.empty());

        StepVerifier.create(bootcampUseCase.registerBootcamp(bootcamp, List.of(capabilityId)))
                .expectNextMatches(saved -> saved.name().equals("Bootcamp Dev"))
                .verifyComplete();

        verify(bootcampPersistencePort).saveBootcamp(any());
        verify(bootcampPersistencePort).saveBootcampCapacity(any(), any());
    }

    @Test
    void registerBootcamp_error_emptyCapabilities() {
        StepVerifier.create(bootcampUseCase.registerBootcamp(bootcamp, List.of()))
                .expectErrorMatches(ex -> ex instanceof DomainException &&
                        ((DomainException) ex).getMessage().equals(Message.RANGE_CAPACITIES.getMessage()))
                .verify();
    }

    @Test
    void registerBootcamp_error_duplicateCapabilities() {
        UUID id = UUID.randomUUID();
        StepVerifier.create(bootcampUseCase.registerBootcamp(bootcamp, List.of(id, id)))
                .expectErrorMatches(ex -> ex instanceof DomainException &&
                        ((DomainException) ex).getMessage().equals(Message.DUPLICATE_CAPACITIES.getMessage()))
                .verify();
    }

    @Test
    void registerBootcamp_error_alreadyExists() {
        when(bootcampPersistencePort.existByName(any())).thenReturn(Mono.just(true));

        StepVerifier.create(bootcampUseCase.registerBootcamp(bootcamp, List.of(capabilityId)))
                .expectErrorMatches(ex -> ex instanceof DomainException &&
                        ((DomainException) ex).getMessage().equals(Message.BOOTCAMP_ALREADY_EXISTS.getMessage()))
                .verify();
    }

    @Test
    void registerBootcamp_error_invalidCapabilities() {
        when(bootcampPersistencePort.existByName(any())).thenReturn(Mono.just(false));
        when(bootcampPersistencePort.validateExistingCapacity(any())).thenReturn(Mono.just(false));

        StepVerifier.create(bootcampUseCase.registerBootcamp(bootcamp, List.of(capabilityId)))
                .expectErrorMatches(ex -> ex instanceof DomainException &&
                        ((DomainException) ex).getMessage().equals(Message.INVALID_CAPACITIES.getMessage()))
                .verify();
    }

    @Test
    void getAllBootcamps_success() {
        BootcampWithCapability bootcampWithCapability = mock(BootcampWithCapability.class);
        when(bootcampPersistencePort.getAllBootcamps(any(), any(), anyInt(), anyInt()))
                .thenReturn(Flux.just(bootcampWithCapability));

        StepVerifier.create(bootcampUseCase.getAllBootcamps("asc", "name", 0, 10))
                .expectNext(bootcampWithCapability)
                .verifyComplete();
    }

    @Test
    void deleteBootcamp_success() {
        when(bootcampPersistencePort.deleteBootcamp(any())).thenReturn(Mono.empty());

        StepVerifier.create(bootcampUseCase.deleteBootcamp(UUID.randomUUID()))
                .verifyComplete();
    }

    @Test
    void getBootcampById_success() {
        when(bootcampPersistencePort.findById(any())).thenReturn(Mono.just(bootcamp));

        StepVerifier.create(bootcampUseCase.getBootcampById(UUID.randomUUID()))
                .expectNext(bootcamp)
                .verifyComplete();
    }
}