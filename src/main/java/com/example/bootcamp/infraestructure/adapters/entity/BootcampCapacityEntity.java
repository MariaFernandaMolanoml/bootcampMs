package com.example.bootcamp.infraestructure.adapters.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table(name = "bootcamp_capabilities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BootcampCapacityEntity {
    @Id
    private UUID id;
    @Column("bootcamp_id")
    private UUID bootcampId;
    @Column("capability_id")
    private UUID capacityId;
}