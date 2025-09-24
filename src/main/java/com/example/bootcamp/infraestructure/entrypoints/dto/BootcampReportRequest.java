package com.example.bootcamp.infraestructure.entrypoints.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BootcampReportRequest {
    private String name;
    private String description;
    private String launchDate;
    private Integer durationInDays;
    private List<String> capabilities;
    private List<String> technologies;
}