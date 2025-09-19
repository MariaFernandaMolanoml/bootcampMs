package com.example.bootcamp.domain.model;

import java.util.UUID;

public record BootcampCapacity(
        UUID bootcampId,
        UUID capacityId
) {}