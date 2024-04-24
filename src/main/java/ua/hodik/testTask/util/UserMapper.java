package ua.hodik.testTask.util;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import ua.hodik.testTask.dto.UserDto;
import ua.hodik.testTask.model.User;

@Component
public class UserMapper {
    private final ModelMapper modelMapper;

    public UserMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public User convertToUser(UserDto userDTO) {
        return modelMapper.map(userDTO, User.class);
    }

    public UserDto convertToUserDto(User user) {
        return modelMapper.map(user, UserDto.class);
    }
}
