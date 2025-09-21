package com.example.bootcamp.infraestructure.entrypoints.dto;


import com.example.bootcamp.domain.model.Technology;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CapacityDTO {
    private UUID id;
    private String name;
    private List<TechnologyDTO> technologies;
}