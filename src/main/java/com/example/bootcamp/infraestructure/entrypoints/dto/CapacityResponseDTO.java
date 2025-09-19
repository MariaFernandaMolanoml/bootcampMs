package com.example.bootcamp.infraestructure.entrypoints.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CapacityResponseDTO {
    private String code;
    private String message;
    private String date;
    private List<CapacityDTO> data;
}