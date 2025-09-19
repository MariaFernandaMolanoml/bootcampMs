package com.example.bootcamp.infraestructure.adapters.mapper;

import com.example.bootcamp.domain.model.BootcampCapacity;
import com.example.bootcamp.infraestructure.adapters.entity.BootcampCapacityEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring")
public interface IBootcampCapacityMapper {
    BootcampCapacity toModel(BootcampCapacityEntity entity);
    BootcampCapacityEntity toEntity(BootcampCapacity model);

}