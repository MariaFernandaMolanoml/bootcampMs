package com.example.bootcamp.infraestructure.adapters.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.relational.core.mapping.Column;

import java.time.LocalDate;
import java.util.UUID;

@Table(name = "bootcamps")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BootcampEntity {
    @Id
    private UUID id;

    @Column("name")
    private String name;
    @Column("description")
    private String description;
    @Column("launch_date")
    private LocalDate launchDate;
    @Column("duration_in_days")
    private int durationInDays;
}
