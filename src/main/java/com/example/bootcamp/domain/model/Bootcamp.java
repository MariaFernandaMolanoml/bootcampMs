package com.example.bootcamp.domain.model;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record Bootcamp(
        UUID id,
        String name,
        String description,
        LocalDate launchDate,
        int durationInDays,
        List<UUID> capabilities
) {}
