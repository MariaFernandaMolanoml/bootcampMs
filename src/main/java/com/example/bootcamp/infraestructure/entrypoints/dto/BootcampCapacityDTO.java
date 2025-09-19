package com.example.bootcamp.infraestructure.entrypoints.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder(toBuilder = true)
public class BootcampCapacityDTO {
    private UUID bootcampId;
    private UUID capabilityId;
}