package org.example.filestorageapi.mapper;

import org.example.filestorageapi.dto.UserAuthDto;
import org.example.filestorageapi.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface UserAuthDtoToUserMapper {

    UserAuthDtoToUserMapper INSTANCE = Mappers.getMapper(UserAuthDtoToUserMapper.class);

    @Mapping(source = "password", target = "encryptedPassword")
    User toEntity(UserAuthDto userAuthDto);
}
