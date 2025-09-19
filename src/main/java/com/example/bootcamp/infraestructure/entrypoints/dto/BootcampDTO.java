package com.example.bootcamp.infraestructure.entrypoints.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record BootcampDTO(
        UUID id,
        String name,
        String description,
        LocalDate launchDate,
        int durationInDays,
        List<UUID> capabilities
) {}
