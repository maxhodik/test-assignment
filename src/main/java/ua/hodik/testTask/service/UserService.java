package ua.hodik.testTask.service;

import com.github.fge.jsonpatch.JsonPatch;
import ua.hodik.testTask.dto.DateFormDto;
import ua.hodik.testTask.dto.UserDto;

import java.util.List;

public interface UserService {
    UserDto patchUpdate(String email, JsonPatch jsonPatch);

    UserDto createUser(UserDto userDTO);

    List<UserDto> findAllUsers();

    UserDto update(String email, UserDto userDto);

    void delete(String email);

    List<UserDto> searchByDateRange(DateFormDto dateForm);
}
