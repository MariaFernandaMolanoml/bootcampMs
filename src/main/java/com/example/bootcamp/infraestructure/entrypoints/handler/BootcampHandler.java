package com.example.bootcamp.infraestructure.entrypoints.handler;

import com.example.bootcamp.domain.api.IBootcampServicePort;
import com.example.bootcamp.domain.exceptions.DomainException;
import com.example.bootcamp.domain.model.Bootcamp;
import com.example.bootcamp.infraestructure.entrypoints.dto.BootcampDTO;
import com.example.bootcamp.infraestructure.entrypoints.mapper.BootcampMapper;
import com.example.bootcamp.infraestructure.entrypoints.util.ApiResponse;
import com.example.bootcamp.infraestructure.entrypoints.util.ErrorDTO;
import com.example.bootcamp.domain.enums.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class BootcampHandler {

    private final IBootcampServicePort bootcampServicePort;
    private final BootcampMapper bootcampMapper;

    public Mono<ServerResponse> createBootcamp(ServerRequest request) {
        return request.bodyToMono(BootcampDTO.class)
                .flatMap(dto -> {
                    Bootcamp bootcamp = bootcampMapper.dtoToModel(dto);
                    List<UUID> capabilities = bootcamp.capabilities(); // getter correcto para record
                    return bootcampServicePort.registerBootcamp(bootcamp, capabilities);
                })
                .flatMap(saved -> {
                    ApiResponse response = ApiResponse.builder()
                            .code(Message.BOOTCAMP_CREATED.getCode())
                            .message(Message.BOOTCAMP_CREATED.getMessage())
                            .data(bootcampMapper.modelToDto(saved))
                            .date(Instant.now().toString())
                            .build();
                    return ServerResponse.status(HttpStatus.CREATED).bodyValue(response);
                })
                .doOnError(ex -> log.error("Error creating bootcamp", ex))
                .onErrorResume(DomainException.class, ex -> {
                    Message msg = ex.getTechnicalMessage() != null ? ex.getTechnicalMessage() : Message.INTERNAL_ERROR;
                    ErrorDTO errorDTO = ErrorDTO.builder()
                            .code(msg.getCode())
                            .message(msg.getMessage())
                            .param(msg.getParam())
                            .build();
                    return buildErrorResponse(HttpStatus.BAD_REQUEST, msg, List.of(errorDTO));
                })
                .onErrorResume(ex -> {
                    ErrorDTO errorDTO = ErrorDTO.builder()
                            .code(Message.INTERNAL_ERROR.getCode())
                            .message(Message.INTERNAL_ERROR.getMessage())
                            .build();
                    return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, Message.INTERNAL_ERROR, List.of(errorDTO));
                });
    }

    private Mono<ServerResponse> buildErrorResponse(HttpStatus status, Message error, List<ErrorDTO> errors) {
        ApiResponse apiErrorResponse = ApiResponse.builder()
                .code(error.getCode())
                .message(error.getMessage())
                .errors(errors)
                .date(Instant.now().toString())
                .build();
        return ServerResponse.status(status).bodyValue(apiErrorResponse);
    }
}
