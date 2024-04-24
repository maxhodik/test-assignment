package ua.hodik.testTask.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Qualifier;
import ua.hodik.testTask.dao.UserDao;
import ua.hodik.testTask.dto.DateFormDto;
import ua.hodik.testTask.dto.UserDto;
import ua.hodik.testTask.exceptions.UserAlreadyExistsException;
import ua.hodik.testTask.exceptions.UserNotFoundException;
import ua.hodik.testTask.exceptions.UserNotUpdatedException;
import ua.hodik.testTask.model.User;
import ua.hodik.testTask.util.UserMapper;
import ua.hodik.testTask.util.UserValidator;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    private static final String EMAIL = "test@gmail.com";
    private static final String EMAIL_TO_UPDATE = "new@gmail.com";

    private static final User USER = createUser(EMAIL);
    private static final User UPDATED_USER = createUser(EMAIL_TO_UPDATE);

    private static final UserDto USER_DTO = createUserDto(EMAIL);
    private static final UserDto UPDATED_USER_DTO = createUserDto(EMAIL_TO_UPDATE);

    private JsonNode userJsonNode;

    private static final List<UserDto> EXPECTED_USER_DTO_LIST = List.of(USER_DTO);
    private static final List<User> EXPECTED_USER_LIST = List.of(USER);

    private static final DateFormDto DATE_FORM = new DateFormDto(LocalDate.now().minusDays(1), LocalDate.now());
    private static final JsonNode JSON_NODE = createJsonPatch();
    private final JsonPatch JSON_PATCH = getJsonPatch();


    @Mock
    private UserDao userDao;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private UserMapper userMapper;
    @Mock
    private UserValidator userValidator;
    @Mock
    private JsonPatch patch;
    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    public void setUp() {
        ObjectMapper objMapper = new ObjectMapper();
        objMapper.registerModule(new JavaTimeModule());
        userJsonNode = objMapper.valueToTree(USER_DTO);
    }

    @Test
    void patchUpdate_ShouldThrowNotFoundException() {
        //given
        when(userDao.findByEmail(EMAIL)).thenReturn(Optional.empty());
        //when
        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> userService.patchUpdate(EMAIL, JSON_PATCH));
        //given
        assertEquals("User with email test@gmail.com not found", exception.getMessage());
    }

    @Test
    void patchUpdate_ShouldThrowJsonException() throws JsonPatchException {
        //given
        when(userDao.findByEmail(EMAIL)).thenReturn(Optional.of(USER));
        when(userMapper.convertToUserDto(USER)).thenReturn(USER_DTO);
        when(objectMapper.convertValue(USER_DTO, JsonNode.class)).thenReturn(userJsonNode);
        when(patch.apply(any())).thenThrow(JsonPatchException.class);
        //when
        UserNotUpdatedException exception = assertThrows(UserNotUpdatedException.class,
                () -> userService.patchUpdate(EMAIL, patch));
        //given
        assertEquals("User with email test@gmail.com not updated", exception.getMessage());
    }

    @Test
    void patchUpdate_ShouldThrowJsonProcessingException() throws JsonProcessingException {
        //given
        when(userDao.findByEmail(EMAIL)).thenReturn(Optional.of(USER));
        when(userMapper.convertToUserDto(USER)).thenReturn(USER_DTO);
        when(objectMapper.convertValue(USER_DTO, JsonNode.class)).thenReturn(userJsonNode);
        when(objectMapper.treeToValue(any(), eq(UserDto.class))).thenThrow(JsonProcessingException.class);
        //when
        UserNotUpdatedException exception = assertThrows(UserNotUpdatedException.class,
                () -> userService.patchUpdate(EMAIL, patch));
        //given
        assertEquals("User with email test@gmail.com not updated", exception.getMessage());
    }

    @Test
    void patchUpdate_Success() throws JsonPatchException, JsonProcessingException {
        //given
        when(userDao.findByEmail(EMAIL)).thenReturn(Optional.of(USER));
        when(userMapper.convertToUserDto(USER)).thenReturn(USER_DTO);
        when(objectMapper.convertValue(USER_DTO, JsonNode.class)).thenReturn(userJsonNode);
        when(objectMapper.treeToValue(any(), eq(UserDto.class))).thenReturn(UPDATED_USER_DTO);
        doNothing().when(userValidator).validate(any(), any());
        when(userDao.update(EMAIL, userMapper.convertToUser(UPDATED_USER_DTO))).thenReturn(UPDATED_USER);
        when(userMapper.convertToUserDto(UPDATED_USER)).thenReturn(UPDATED_USER_DTO);
        //when
        UserDto userDto = userService.patchUpdate(EMAIL, JSON_PATCH);
        //given
        assertEquals(UPDATED_USER_DTO, userDto);
    }

    @Test
    void createUser_Success() {
        //given
        when(userDao.findByEmail(EMAIL)).thenReturn(Optional.empty());
        when(userMapper.convertToUser(USER_DTO)).thenReturn(USER);
        when(userDao.create(any())).thenReturn(USER);
        when(userMapper.convertToUserDto(USER)).thenReturn(USER_DTO);
        //when
        UserDto userDto = userService.createUser(USER_DTO);
        //then
        assertEquals(USER_DTO, userDto);
    }

    @Test
    void createUser_ShouldTrowException() {
        //given
        when(userDao.findByEmail(EMAIL)).thenReturn(Optional.of(USER));
        //when
        UserAlreadyExistsException exception = assertThrows(UserAlreadyExistsException.class, () ->
                userService.createUser(USER_DTO));
        String message = exception.getMessage();
        //then
        assertEquals("User with email test@gmail.com already exists ", message);

    }

    @Test
    void ShouldReturnListUsersDto() {
        //given
        when(userDao.findAll()).thenReturn(EXPECTED_USER_LIST);
        when(userMapper.convertToUserDto(USER)).thenReturn(USER_DTO);
        //when
        List<UserDto> usersDto = userService.findAllUsers();
        //then
        assertEquals(EXPECTED_USER_DTO_LIST, usersDto);
    }

    @Test
    void ShouldUpdateUser() {
        //given
        when(userDao.findByEmail(EMAIL)).thenReturn(Optional.of(USER));
        when(userDao.update(eq(EMAIL), any())).thenReturn(UPDATED_USER);
        when(userMapper.convertToUserDto(UPDATED_USER)).thenReturn(UPDATED_USER_DTO);
        //when
        UserDto updatedUserDto = userService.update(EMAIL, USER_DTO);
        //then
        assertEquals(UPDATED_USER_DTO, updatedUserDto);
    }

    @Test
    void Should_TrowException() {
        //given
        when(userDao.findByEmail(EMAIL)).thenReturn(Optional.empty());
        //when
        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> userService.update(EMAIL, USER_DTO));
        //then
        assertEquals("User with email test@gmail.com not found", exception.getMessage());
    }

    @Test
    void Should_TrowExceptionWhenUserIsAbsent() {
        //given
        when(userDao.findByEmail(EMAIL)).thenReturn(Optional.empty());
        //when
        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> userService.delete(EMAIL));
        //then
        assertEquals("User with email test@gmail.com not found", exception.getMessage());
    }

    @Test
    void ShouldDeleteUserByEmail() {
        //given
        when(userDao.findByEmail(EMAIL)).thenReturn(Optional.of(USER));
        //when
        userService.delete(EMAIL);
        //then
        verify(userDao).delete(EMAIL);
    }

    @Test
    void searchByDateRange() {
        //given
        when(userDao.searchByBirthDayRange(any(), any())).thenReturn(EXPECTED_USER_LIST);
        when(userMapper.convertToUserDto(USER)).thenReturn(USER_DTO);
        //when
        List<UserDto> userDtoList = userService.searchByDateRange(DATE_FORM);
        //then
        assertEquals(EXPECTED_USER_DTO_LIST, userDtoList);
    }

    private static User createUser(String email) {
        User user = new User();
        user.setEmail(email);
        user.setFirstName("John");
        user.setLastName("Obama");
        user.setBirthDate(LocalDate.of(2000, 1, 1));
        user.setAddress("Kyiv, 25");
        user.setPhoneNumber("+1234567890");
        return user;
    }

    private static UserDto createUserDto(String email) {
        UserDto userDto = new UserDto();
        userDto.setEmail(email);
        userDto.setFirstName("John");
        userDto.setLastName("Obama");
        userDto.setBirthDate(LocalDate.of(2000, 1, 1));
        userDto.setAddress("Kyiv, 25");
        userDto.setPhoneNumber("+1234567890");
        return userDto;
    }

    private static JsonNode createJsonPatch() {
        ObjectNode patchOperations = JsonNodeFactory.instance.objectNode();
        patchOperations.putArray("operations")
                .addObject()
                .put("op", "replace")
                .put("path", "/email")
                .put("value", "new@gmail.com");
        JsonNode jsonNode = patchOperations.get("operations");
        return jsonNode;
    }

    private JsonPatch getJsonPatch() {
        final JsonPatch JSON_PATCH;
        try {
            JSON_PATCH = JsonPatch.fromJson(JSON_NODE);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return JSON_PATCH;
    }
}