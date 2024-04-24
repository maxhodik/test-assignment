package ua.hodik.testTask.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import ua.hodik.testTask.dao.UserDao;
import ua.hodik.testTask.dto.DateFormDto;
import ua.hodik.testTask.dto.UserDto;
import ua.hodik.testTask.exceptions.UserAlreadyExistsException;
import ua.hodik.testTask.exceptions.UserNotFoundException;
import ua.hodik.testTask.exceptions.UserNotUpdatedException;
import ua.hodik.testTask.model.User;
import ua.hodik.testTask.util.AbstractValidator;
import ua.hodik.testTask.util.UserMapper;

import java.time.LocalDate;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    private final UserDao userDao;
    private final ObjectMapper objectMapper;
    private final UserMapper userMapper;
    private final AbstractValidator userValidator;


    public UserServiceImpl(UserDao userDao, ObjectMapper objectMapper, UserMapper userMapper,
                           @Qualifier("userValidator") AbstractValidator userValidator) {
        this.userDao = userDao;
        this.objectMapper = objectMapper;
        this.userMapper = userMapper;
        this.userValidator = userValidator;
    }


    @Override
    public UserDto patchUpdate(String email, JsonPatch jsonPatch) {
        User user = userDao.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(String.format("User with email %s not found", email)));
        UserDto userDtoToUpdate = patchUser(email, jsonPatch, user);
        BindingResult errors = new BeanPropertyBindingResult(userDtoToUpdate, "userDtoToUpdate");
        userValidator.validate(userDtoToUpdate, errors);
        return update(email, userDtoToUpdate);

    }

    @Override
    public UserDto createUser(UserDto userDTO) {
        if (userDao.findByEmail(userDTO.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException(String.format("User with email %s already exists ", userDTO.getEmail()));
        }
        User user = userDao.create(userMapper.convertToUser(userDTO));
        return userMapper.convertToUserDto(user);
    }

    @Override
    public List<UserDto> findAllUsers() {
        List<User> users = userDao.findAll();
        return getUserDtoList(users);
    }

    @Override
    public UserDto update(String email, UserDto userDto) {
        isUserExists(email);
        User updatedUser = userDao.update(email, userMapper.convertToUser(userDto));
        return userMapper.convertToUserDto(updatedUser);
    }

    @Override
    public void delete(String email) {
        isUserExists(email);
        userDao.delete(email);
    }

    @Override
    public List<UserDto> searchByDateRange(DateFormDto dateForm) {
        LocalDate from = dateForm.getFrom();
        LocalDate to = dateForm.getTo();
        List<User> users = userDao.searchByBirthDayRange(from, to);
        return getUserDtoList(users);
    }

    private UserDto patchUser(String email, JsonPatch jsonPatch, User user) {
        UserDto userDto = userMapper.convertToUserDto(user);
        UserDto userDtoToUpdate;
        try {
            userDtoToUpdate = applyPatchToUser(jsonPatch, userDto);
        } catch (JsonPatchException | JsonProcessingException e) {
            throw new UserNotUpdatedException(String.format("User with email %s not updated", email), e);
        }
        return userDtoToUpdate;
    }

    private UserDto applyPatchToUser(JsonPatch patch, UserDto targetUser) throws JsonPatchException, JsonProcessingException {
        JsonNode node = objectMapper.convertValue(targetUser, JsonNode.class);
        JsonNode patched = patch.apply(node);
        return objectMapper.treeToValue(patched, UserDto.class);
    }

    private List<UserDto> getUserDtoList(List<User> users) {
        return users.stream().map(userMapper::convertToUserDto).toList();
    }

    private void isUserExists(String email) {
        userDao.findByEmail(email).orElseThrow(() -> new UserNotFoundException(String.format("User with email %s not found", email)));
    }
}
