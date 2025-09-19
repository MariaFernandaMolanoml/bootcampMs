package com.example.bootcamp.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Message {
    RANGE_CAPACITIES("403", "Un bootcamp debe tener mínimo 1 capacidad y máximo 4 capacidades asociadas", ""),
    BOOTCAMP_ALREADY_EXISTS("400", "Este bootcamp ya se encuentra registrado", "nombre"),
    DUPLICATE_CAPACITIES("400", "Hay una o varias capacidades repetidas", ""),
    INVALID_CAPACITIES("400", "Una o varias capacidades no son válidas", ""),
    INTERNAL_ERROR("500", "Error interno del servidor", ""),
    BOOTCAMP_CREATED("201", "Bootcamp creado exitosamente", "");

    private final String code;
    private final String message;
    private final String param;
}
