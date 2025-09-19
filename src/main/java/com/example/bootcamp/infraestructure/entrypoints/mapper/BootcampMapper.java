package com.example.bootcamp.infraestructure.entrypoints.mapper;

import com.example.bootcamp.domain.model.Bootcamp;
import com.example.bootcamp.infraestructure.entrypoints.dto.BootcampDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BootcampMapper {

    @Mapping(source = "id", target = "id")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "launchDate", target = "launchDate")
    @Mapping(source = "durationInDays", target = "durationInDays")
    @Mapping(source = "capabilities", target = "capabilities")
    Bootcamp dtoToModel(BootcampDTO dto);
    BootcampDTO modelToDto(Bootcamp model);
}
