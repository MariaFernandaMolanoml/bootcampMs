package com.example.bootcamp.domain.model;

import java.util.List;
import java.util.UUID;

public record Capability(
        UUID id,
        String name,
        List<Technology> technologies
) {}
