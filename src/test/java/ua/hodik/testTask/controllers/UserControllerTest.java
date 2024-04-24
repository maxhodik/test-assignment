package ua.hodik.testTask.controllers;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ua.hodik.testTask.TestConfiguration;
import ua.hodik.testTask.dto.DateFormDto;
import ua.hodik.testTask.dto.UserDto;
import ua.hodik.testTask.exceptions.UserAlreadyExistsException;
import ua.hodik.testTask.exceptions.UserNotFoundException;
import ua.hodik.testTask.exceptions.UserNotUpdatedException;
import ua.hodik.testTask.model.User;
import ua.hodik.testTask.service.UserService;
import ua.hodik.testTask.util.AbstractValidator;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = UserController.class)
@Import(TestConfiguration.class)
class UserControllerTest {
    public static final String USER_WITH_EMAIL_TEST_GMAIL_COM_ALREADY_EXISTS = "User with email test@gmail.com already exists ";
    public static final String FIRST_NAME_SHOULD_NOT_BE_EMPTY = "firstName - Should not be empty;";
    public static final String FIRST_NAME_SHOULD_NOT_BE_EMPTY_BIRTH_DATE_YOU_ARE_TOO_YOUNG = "firstName - Should not be empty;birthDate - You are too young!!!;";
    public static final String USER_WITH_EMAIL_TEST_GMAIL_COM_NOT_FOUND = "User with email test@gmail.com not found";
    public static final String USER_WITH_EMAIL_NONEXISTENT_GMAIL_COM_NOT_FOUND = "User with email nonexistent@gmail.com not found";
    public static final String USER_WITH_EMAIL_USER_EXAMPLE_COM_NOT_UPDATED = "User with email user@example.com not updated";
    public static final String MESSAGE = "$.message";
    public static final String $_EMAIL = "$.email";
    public static final String $_FIRST_NAME = "$.firstName";
    public static final String $_LAST_NAME = "$.lastName";
    public static final String $_BIRTH_DATE = "$.birthDate";
    public static final String $_ADDRESS = "$.address";
    public static final String $_PHONE_NUMBER = "$.phoneNumber";
    private UserDto userDto;
    private static final JsonNode JSON_NODE = createJsonPatch();
    private static final JsonNode JSON_INCORRECT_NODE = createIncorrectJsonPatch();
    private static final String EMAIL = "test@gmail.com";
    private static final String UPDATED_EMAIL = "new@gmail.com";
    private static final UserDto UPDATED_USER_DTO = createUserDto(UPDATED_EMAIL);
    private static final String FIRST_NAME = "John";
    private static final String LAST_NAME = "Obama";

    private static final LocalDate BIRTH_DATE = LocalDate.of(2000, 1, 1);
    private static final String ADDRESS = "Kyiv, 25";
    private static final String PHONE_NUMBER = "+1234567890";

    private static final DateFormDto DATE_FORM = new DateFormDto(LocalDate.now().minusDays(1), LocalDate.now());


    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private UserService userService;

    @SpyBean(name = "userValidator")
    @Qualifier("userValidator")
    private AbstractValidator userValidator;

    @SpyBean(name = "dateValidator")
    @Qualifier("dateValidator")
    private AbstractValidator dateValidator;


    @BeforeEach
    public void setup() {
        userDto = createUserDto(EMAIL);
    }

    @Test
    void createUser() throws Exception {
        mvc.perform(get("/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testCreateUser_Success() throws Exception {
        //given
        doCallRealMethod().when(userValidator).validate(any(), any());
        when(userService.createUser(userDto)).thenReturn(userDto);
        //then
        mvc.perform(MockMvcRequestBuilders
                        .post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath($_EMAIL).value(EMAIL))
                .andExpect(jsonPath($_FIRST_NAME).value(FIRST_NAME))
                .andExpect(jsonPath($_LAST_NAME).value(LAST_NAME))
                .andExpect(jsonPath($_BIRTH_DATE).value("01.01.2000"))
                .andExpect(jsonPath($_ADDRESS).value(ADDRESS))
                .andExpect(jsonPath($_PHONE_NUMBER).value(PHONE_NUMBER))
                .andReturn();
    }

    @Test
    void testCreateUser_ShouldTrowException() throws Exception {
        //given
        doCallRealMethod().when(userValidator).validate(any(), any());
        UserAlreadyExistsException userAlreadyExistsException =
                new UserAlreadyExistsException(USER_WITH_EMAIL_TEST_GMAIL_COM_ALREADY_EXISTS);

        when(userService.createUser(userDto)).thenThrow(userAlreadyExistsException);
        //then
        mvc.perform(MockMvcRequestBuilders
                        .post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath(MESSAGE).value(USER_WITH_EMAIL_TEST_GMAIL_COM_ALREADY_EXISTS))
                .andReturn();
    }


    @Test
    void testCreateUser_InvalidDate() throws Exception {
        //given
        userDto.setFirstName(null);
        doCallRealMethod().when(userValidator).validate(any(), any());
        //when then
        mvc.perform(MockMvcRequestBuilders
                        .post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath(MESSAGE).value(FIRST_NAME_SHOULD_NOT_BE_EMPTY))
                .andReturn();
    }

    @Test
    void testCreateUser_InvalidData() throws Exception {
        //given
        userDto.setFirstName(null);
        userDto.setBirthDate(LocalDate.now().minusYears(17));
        doCallRealMethod().when(userValidator).validate(any(), any());
        //then
        mvc.perform(MockMvcRequestBuilders
                        .post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath(MESSAGE).value(FIRST_NAME_SHOULD_NOT_BE_EMPTY_BIRTH_DATE_YOU_ARE_TOO_YOUNG))
                .andReturn();
    }

    @Test
    void testUpdateUser_Success() throws Exception {
        //given
        doCallRealMethod().when(userValidator).validate(any(), any());
        when(userService.update(any(), any())).thenReturn(UPDATED_USER_DTO);
        //when then
        mvc.perform(MockMvcRequestBuilders
                        .put("/users/test@gmail.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath($_EMAIL).value(UPDATED_EMAIL))
                .andExpect(jsonPath($_FIRST_NAME).value(FIRST_NAME))
                .andExpect(jsonPath($_LAST_NAME).value(LAST_NAME))
                .andExpect(jsonPath($_BIRTH_DATE).value("01.01.2000"))
                .andExpect(jsonPath($_ADDRESS).value(ADDRESS))
                .andExpect(jsonPath($_PHONE_NUMBER).value(PHONE_NUMBER))
                .andReturn();
    }


    @Test
    void testUpdateUser_UserNotFound() throws Exception {
        //given
        UserNotFoundException userNotFoundException =
                new UserNotFoundException(USER_WITH_EMAIL_TEST_GMAIL_COM_NOT_FOUND);
        userDto.setEmail(EMAIL);
        when(userService.update(EMAIL, userDto)).thenThrow(userNotFoundException);
        //when then
        mvc.perform(MockMvcRequestBuilders
                        .put("/users/test@gmail.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(jsonPath(MESSAGE).value(USER_WITH_EMAIL_TEST_GMAIL_COM_NOT_FOUND))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateUser_ValidationErrors() throws Exception {
        //given
        userDto.setLastName(null);
        doCallRealMethod().when(userValidator).validate(any(), any());
        //when then
        mvc.perform(MockMvcRequestBuilders
                        .put("/users/test@gmail.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testDeleteUser_Success() throws Exception {

        doNothing().when(userService).delete(EMAIL);

        mvc.perform(MockMvcRequestBuilders
                        .delete("/users/test@gmail.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    void testDeleteUser_UserNotFound() throws Exception {
        // given
        UserNotFoundException userNotFoundException =
                new UserNotFoundException(USER_WITH_EMAIL_TEST_GMAIL_COM_NOT_FOUND);
        doThrow(userNotFoundException).when(userService).delete(EMAIL);
        //when
        //then
        mvc.perform(MockMvcRequestBuilders
                        .delete("/users/test@gmail.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(jsonPath(MESSAGE).value(USER_WITH_EMAIL_TEST_GMAIL_COM_NOT_FOUND))
                .andExpect(status().isNotFound());
    }

    @Test
    void testSearchByDateRange_ValidRequest() throws Exception {
        // given
        List<UserDto> userDtoList = Arrays.asList(new UserDto(), new UserDto());
        when(userService.searchByDateRange(any())).thenReturn(userDtoList);
        doCallRealMethod().when(dateValidator).validate(any(), any());
        // when then
        mvc.perform(MockMvcRequestBuilders
                        .post("/users/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(DATE_FORM)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void testSearchByDateRange_InvalidDateForm() throws Exception {
        //given
        DateFormDto dateForm = new DateFormDto(LocalDate.now(), LocalDate.now().minusDays(1));
        doCallRealMethod().when(dateValidator).validate(any(), any());
        //when
        //then
        mvc.perform(MockMvcRequestBuilders
                        .post("/users/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dateForm)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath(MESSAGE).value("from - 'From' date should be before 'to' date;"));
    }

    @Test
    void testSearchByDateRange_ValidEmptyResult() throws Exception {
        //given
        DateFormDto dateForm = new DateFormDto(LocalDate.now().minusDays(1), LocalDate.now());
        when(userService.searchByDateRange(dateForm)).thenReturn(Collections.emptyList());
        //when
        //then
        mvc.perform(MockMvcRequestBuilders
                        .post("/users/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dateForm)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void testSearchByDateRange_InvalidDateFormFormat() throws Exception {
        String invalidDateFormJson = "{\"from\": \"2022-10-12\", \"to\": \"2023-10-12\"}";
        doCallRealMethod().when(dateValidator).validate(any(), any());
        mvc.perform(MockMvcRequestBuilders
                        .post("/users/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDateFormJson)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void patchUpdate_ExistingUserEmailPatched_ReturnsUpdatedUser() throws Exception {
        // given
        String email = EMAIL;
        doCallRealMethod().when(userValidator).validate(any(), any());
        when(userService.patchUpdate(eq(email), any())).thenReturn(UPDATED_USER_DTO);
        // when
        // then
        mvc.perform(MockMvcRequestBuilders
                        .patch("/users/{email}", email)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_NODE.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath($_EMAIL).value(UPDATED_EMAIL));
    }

    @Test
    void patchUpdate_NonExistentUser_ReturnsNotFound() throws Exception {
        // given
        UserNotFoundException userNotFoundException =
                new UserNotFoundException(USER_WITH_EMAIL_NONEXISTENT_GMAIL_COM_NOT_FOUND);
        String email = "nonexistent@gmail.com";
        when(userService.patchUpdate(eq(email), any())).thenThrow(userNotFoundException);
        // when
        // then
        mvc.perform(MockMvcRequestBuilders
                        .patch("/users/{email}", email)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_NODE.toString()))
                .andExpect(jsonPath(MESSAGE).value(USER_WITH_EMAIL_NONEXISTENT_GMAIL_COM_NOT_FOUND))
                .andExpect(status().isNotFound());
    }

    @Test
    void patchUpdate_NonExistentUser_ReturnsNotUpdated() throws Exception {
        // given
        UserNotUpdatedException userNotUpdatedException =
                new UserNotUpdatedException(USER_WITH_EMAIL_USER_EXAMPLE_COM_NOT_UPDATED);
        String email = "user@example.com";
        when(userService.patchUpdate(eq(email), any())).thenThrow(userNotUpdatedException);

        //when then
        mvc.perform(MockMvcRequestBuilders
                        .patch("/users/{email}", email)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_INCORRECT_NODE.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath(MESSAGE).value(USER_WITH_EMAIL_USER_EXAMPLE_COM_NOT_UPDATED));
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

    private static User createTestUser() {
        User user = new User();
        user.setEmail("test@gmail.com");
        user.setFirstName("John");
        user.setLastName("Obama");
        user.setBirthDate(LocalDate.of(2000, 1, 1));
        user.setAddress("Kyiv, 25");
        user.setPhoneNumber("+1234567890");
        return user;
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

    private static JsonNode createIncorrectJsonPatch() {
        ObjectNode patchOperations = JsonNodeFactory.instance.objectNode();
        patchOperations.putArray("operations")
                .addObject()
                .put("op", "replace")
                .put("path", "/wrong")
                .put("value", "new@gmail.com");

        JsonNode jsonNode = patchOperations.get("operations");
        return jsonNode;
    }
}
