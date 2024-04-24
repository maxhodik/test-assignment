package ua.hodik.testTask.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import jakarta.validation.Valid;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.*;
import ua.hodik.testTask.dao.UserDao;
import ua.hodik.testTask.dto.DateFormDto;
import ua.hodik.testTask.dto.UserDto;
import ua.hodik.testTask.exceptions.InvalidDataException;
import ua.hodik.testTask.exceptions.UserAlreadyExistsException;
import ua.hodik.testTask.exceptions.UserNotFoundException;
import ua.hodik.testTask.exceptions.UserNotUpdatedException;
import ua.hodik.testTask.model.User;
import ua.hodik.testTask.util.UserMapper;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;

    private final Validator userValidator;
    private final Validator dateValidator;

    private final UserDao userDao;

    @Autowired
    public UserController(UserMapper userMapper, ObjectMapper objectMapper,
                          @Qualifier("userValidator") Validator userValidator,
                          @Qualifier("dateValidator") Validator dateValidator, UserDao userDao) {
        this.userMapper = userMapper;
        this.objectMapper = objectMapper;
        this.userValidator = userValidator;
        this.dateValidator = dateValidator;
        this.userDao = userDao;
    }


    @PostMapping
    public ResponseEntity<UserDto> createUser(@RequestBody UserDto userDTO, BindingResult bindingResult) {
        validateUser(userDTO, bindingResult);

        if (userDao.findByEmail(userDTO.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException(String.format("User with email %s already exists ", userDTO.getEmail()));
        }
        User user = userDao.create(userMapper.convertToUser(userDTO));
        UserDto userDto = userMapper.convertToUserDto(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(userDto);
    }

    @GetMapping
    public ResponseEntity<List<UserDto>> getAll() {
        List<User> users = userDao.findAll();
        return ResponseEntity.ok(getUserDtoList(users));
    }

    @PatchMapping("/{email}")
    public ResponseEntity<UserDto> patchUpdate(@PathVariable String email, @RequestBody JsonPatch jsonPatch, BindingResult bindingResult) {
        User user = userDao.findByEmail(email).orElseThrow(() -> new UserNotFoundException(String.format("User with email %s not found", email)));
        UserDto userDto = userMapper.convertToUserDto(user);
        UserDto userDtoToUpdate;
        try {
            userDtoToUpdate = applyPatchToUser(jsonPatch, userDto);
        } catch (JsonPatchException | JsonProcessingException e) {
            throw new UserNotUpdatedException(String.format("User with email %s not updated", email), e);
        }

        validateUser(userDtoToUpdate, bindingResult);
        User userToUpdate = userMapper.convertToUser(userDtoToUpdate);
        User updatedUser = userDao.update(email, userToUpdate);
        UserDto updatedUserDto = userMapper.convertToUserDto(updatedUser);
        return ResponseEntity.status(HttpStatus.OK).body(updatedUserDto);

    }

    @PutMapping("/{email}")
    public ResponseEntity<UserDto> update(@PathVariable String email, @RequestBody UserDto userDto, BindingResult bindingResult) {
        validateUser(userDto, bindingResult);
        isUserExists(email);
        User updatedUser = userDao.update(email, userMapper.convertToUser(userDto));
        userDto = userMapper.convertToUserDto(updatedUser);
        return ResponseEntity.status(HttpStatus.OK).body(userDto);
    }

    @DeleteMapping("/{email}")
    public ResponseEntity<?> remove(@PathVariable String email) {
        isUserExists(email);
        userDao.delete(email);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/search")
    public ResponseEntity<List<UserDto>> searchByDateRange(@RequestBody @Valid DateFormDto dateForm, BindingResult bindingResult) {
    dateValidator.validate(dateForm, bindingResult);
        bindErrors(bindingResult);
        LocalDate from = dateForm.getFrom();
        LocalDate to = dateForm.getTo();
        List<User> users = userDao.searchByBirthDayRange(from, to);
        return ResponseEntity.ok(getUserDtoList(users));
    }

    @NotNull
    private List<UserDto> getUserDtoList(List<User> users) {
        return users.stream().map(userMapper::convertToUserDto).toList();
    }

    private UserDto applyPatchToUser(JsonPatch patch, UserDto targetUser) throws JsonPatchException, JsonProcessingException {
        JsonNode patched = patch.apply(objectMapper.convertValue(targetUser, JsonNode.class));
        return objectMapper.treeToValue(patched, UserDto.class);
    }

    private void isUserExists(String email) {
        userDao.findByEmail(email).orElseThrow(() -> new UserNotFoundException(String.format("User with email %s not found", email)));
    }

    private void validateUser(UserDto userDTO, BindingResult bindingResult) {
        userValidator.validate(userDTO, bindingResult);
        bindErrors(bindingResult);
    }

    private void bindErrors(BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            StringBuilder errorMsg = new StringBuilder();
            List<FieldError> errors = bindingResult.getFieldErrors();
            for (FieldError error : errors) {
                errorMsg.append(error.getField()).append(" - ").append(error.getDefaultMessage()).append(";");
            }
            throw new InvalidDataException(errorMsg.toString());
        }
    }
}
