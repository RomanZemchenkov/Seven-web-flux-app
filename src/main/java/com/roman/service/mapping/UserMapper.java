package com.roman.service.mapping;

import com.roman.dao.entity.User;
import com.roman.service.dto.user.CreateUserDto;
import com.roman.service.dto.user.ShowUserDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "username", source = "username")
    @Mapping(target = "email", source = "email")
    ShowUserDto mapToShow(User user);


    @Mapping(target = "username", source = "username")
    @Mapping(target = "email", source = "email")
    User mapToUser(CreateUserDto dto);

}
