package ua.hodik.testTask.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.*;
import ua.hodik.testTask.dao.UserDao;
import ua.hodik.testTask.dto.DateFormDto;
import ua.hodik.testTask.dto.UserDto;
import ua.hodik.testTask.service.UserService;
import ua.hodik.testTask.util.UserMapper;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;

    private final Validator userValidator;
    private final Validator dateValidator;

    private final UserDao userDao;
    private final UserService userService;

    @Autowired
    public UserController(UserMapper userMapper, ObjectMapper objectMapper,
                          @Qualifier("userValidator") Validator userValidator,
                          @Qualifier("dateValidator") Validator dateValidator, UserDao userDao, UserService userService) {
        this.userMapper = userMapper;
        this.objectMapper = objectMapper;
        this.userValidator = userValidator;
        this.dateValidator = dateValidator;
        this.userDao = userDao;
        this.userService = userService;
    }


    @PostMapping
    public ResponseEntity<UserDto> createUser(@RequestBody UserDto userDTO, BindingResult bindingResult) {
        validateUser(userDTO, bindingResult);
        UserDto userDto = userService.createUser(userDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(userDto);
    }

    @GetMapping
    public ResponseEntity<List<UserDto>> getAll() {
        List<UserDto> userDtoList = userService.findAllUsers();
        return ResponseEntity.ok(userDtoList);
    }

    @PatchMapping("/{email}")
    public ResponseEntity<UserDto> patchUpdate(@PathVariable String email, @RequestBody JsonPatch jsonPatch) {
        UserDto updatedUserDto = userService.patchUpdate(email, jsonPatch);
        return ResponseEntity.status(HttpStatus.OK).body(updatedUserDto);
    }

    @PutMapping("/{email}")
    public ResponseEntity<UserDto> update(@PathVariable String email, @RequestBody UserDto userDto, BindingResult bindingResult) {
        validateUser(userDto, bindingResult);
        UserDto updatedUserDto = userService.update(email, userDto);
        return ResponseEntity.status(HttpStatus.OK).body(updatedUserDto);
    }

    @DeleteMapping("/{email}")
    public ResponseEntity<?> remove(@PathVariable String email) {
        userService.delete(email);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/search")
    public ResponseEntity<List<UserDto>> searchByDateRange(@RequestBody @Valid DateFormDto dateForm, BindingResult bindingResult) {
        dateValidator.validate(dateForm, bindingResult);
        List<UserDto> userDtoList = userService.searchByDateRange(dateForm);
        return ResponseEntity.ok(userDtoList);
    }


    private void validateUser(UserDto userDTO, BindingResult bindingResult) {
        userValidator.validate(userDTO, bindingResult);

    }


}
