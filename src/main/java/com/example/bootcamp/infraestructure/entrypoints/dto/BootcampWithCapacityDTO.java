package com.example.bootcamp.infraestructure.entrypoints.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder(toBuilder = true)
public class BootcampWithCapacityDTO {
    private UUID id;
    private String name;
    private String description;
    private LocalDate launchDate;
    private int durationInDays;
    private List<CapacityDTO> capabilities;
}