package com.example.bootcamp.infraestructure.entrypoints;

import com.example.bootcamp.infraestructure.entrypoints.dto.BootcampDTO;
import com.example.bootcamp.infraestructure.entrypoints.handler.BootcampHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class RouterRest {

    @Bean
    @RouterOperations({
            @RouterOperation(
                    path = "/bootcamps",
                    method = RequestMethod.POST,
                    operation = @Operation(
                            operationId = "createBootcamp",
                            summary = "Crear bootcamp",
                            requestBody = @RequestBody(
                                    required = true,
                                    content = @Content(schema = @Schema(implementation = BootcampDTO.class))
                            ),
                            responses = {
                                    @ApiResponse(responseCode = "201", description = "Bootcamp creado",
                                            content = @Content(schema = @Schema(implementation = BootcampDTO.class))),
                                    @ApiResponse(responseCode = "400", description = "Error de validación")
                            }
                    )
            ),
            @RouterOperation(
                    path = "/bootcamps",
                    method = RequestMethod.GET,
                    operation = @Operation(
                            operationId = "getAllBootcamps",
                            summary = "Listar bootcamps",
                            responses = @ApiResponse(
                                    responseCode = "200",
                                    description = "Lista de bootcamps",
                                    content = @Content(schema = @Schema(implementation = BootcampDTO.class))
                            )
                    )
            ),
            @RouterOperation(
                    path = "/bootcamps/{id}",
                    method = RequestMethod.GET,
                    operation = @Operation(
                            operationId = "getBootcampById",
                            summary = "Obtener bootcamp por id",
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Bootcamp encontrado",
                                            content = @Content(schema = @Schema(implementation = BootcampDTO.class))),
                                    @ApiResponse(responseCode = "404", description = "Bootcamp no encontrado")
                            }
                    )
            ),
            @RouterOperation(
                    path = "/bootcamps/{id}",
                    method = RequestMethod.DELETE,
                    operation = @Operation(
                            operationId = "deleteBootcamp",
                            summary = "Eliminar bootcamp",
                            responses = {
                                    @ApiResponse(responseCode = "204", description = "Bootcamp eliminado"),
                                    @ApiResponse(responseCode = "404", description = "Bootcamp no encontrado")
                            }
                    )
            )
    })
    public RouterFunction<ServerResponse> routerFunction(BootcampHandler handler) {
        return route(POST("/bootcamps"), handler::createBootcamp)
                .andRoute(GET("/bootcamps"), handler::getAllBootcamps)
                .andRoute(GET("/bootcamps/{id}"), handler::getBootcampById)
                .andRoute(DELETE("/bootcamps/{id}"), handler::deleteBootcamp);
    }
}
