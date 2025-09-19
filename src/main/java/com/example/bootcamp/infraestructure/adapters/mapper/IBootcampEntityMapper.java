package com.example.bootcamp.infraestructure.adapters.mapper;

import com.example.bootcamp.domain.model.Bootcamp;
import com.example.bootcamp.infraestructure.adapters.entity.BootcampEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface IBootcampEntityMapper {
    @Mapping(source = "id", target = "id", ignore = true)
    @Mapping(source = "name", target = "name")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "launchDate", target = "launchDate")
    @Mapping(source = "durationInDays", target = "durationInDays")
    Bootcamp toModel(BootcampEntity entity);
    BootcampEntity toEntity(Bootcamp model);
}
