package ua.hodik.testTask.controllers;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;
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
import org.springframework.validation.Validator;
import ua.hodik.testTask.TestConfiguration;
import ua.hodik.testTask.dao.UserDao;
import ua.hodik.testTask.dto.DateFormDto;
import ua.hodik.testTask.dto.UserDto;
import ua.hodik.testTask.model.User;
import ua.hodik.testTask.util.UserMapper;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = UserController.class)
@Import(TestConfiguration.class)
class UserControllerTest {
    private UserDto userDto;
    private static final UserDto UPDATED_USER_DTO = createUpdatedUserDto();
    private static final JsonNode JSON_NODE = createJsonPatch();
    private static final JsonNode JSON_INCORRECT_NODE = createIncorrectJsonPatch();

    private final User user = createTestUser();

    private final DateFormDto dateForm = new DateFormDto(LocalDate.now().minusDays(1), LocalDate.now());
    private final Gson gson = new Gson();
//    private final JsonNode jsonPatch = gson.fromJson(TestUtils.readResource("user.to.update.json"), JsonNode.class);

    //    @Spy
//    @InjectMocks
//

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private UserDao userDao;
    @MockBean
    private UserMapper userMapper;
    @SpyBean(name = "userValidator")
    @Qualifier("userValidator")
    private Validator userValidator;

    @SpyBean(name = "dateValidator")
    @Qualifier("dateValidator")
    private Validator dateValidator;

    @Autowired
    private UserController controller;

    @BeforeEach
    public void setup() {
        userDto = createTestUserDto();
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
        User user = new User();
        when(userMapper.convertToUser(any(UserDto.class))).thenReturn(user);
        when(userDao.findByEmail(userDto.getEmail())).thenReturn(Optional.empty());
        when(userDao.create(user)).thenReturn(user);
        when(userMapper.convertToUserDto(user)).thenReturn(userDto);
        //then
        mvc.perform(MockMvcRequestBuilders
                        .post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.email").value("test@gmail.com"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Obama"))
                .andExpect(jsonPath("$.birthDate").value("01.01.2000"))
                .andExpect(jsonPath("$.address").value("Kyiv, 25"))
                .andExpect(jsonPath("$.phoneNumber").value("+1234567890"))
                .andReturn();
    }

    @Test
    void testCreateUser_ShouldTrowException() throws Exception {
        //given
        User user = new User();
        when(userMapper.convertToUser(any(UserDto.class))).thenReturn(user);
        when(userDao.findByEmail(userDto.getEmail())).thenReturn(Optional.of(user));

        //then
        mvc.perform(MockMvcRequestBuilders
                        .post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("User with email test@gmail.com already exists "))
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
                .andExpect(jsonPath("$.message").value("FirstName - Should not be empty;"))
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
                .andExpect(jsonPath("$.message").value("FirstName - Should not be empty;birthDate - You are too young!!!;"))
                .andReturn();
    }

    @Test
    void testUpdateUser_Success() throws Exception {
        //given
        User user = new User();
        when(userMapper.convertToUser(any(UserDto.class))).thenReturn(user);
        when(userDao.findByEmail("test@gmail.com")).thenReturn(Optional.of(user));
        when(userDao.update("test@gmail.com", user)).thenReturn(user);
        when(userMapper.convertToUserDto(user)).thenReturn(userDto);
        //when then
        mvc.perform(MockMvcRequestBuilders
                        .put("/users/test@gmail.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.email").value("test@gmail.com")) // Check the response body
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Obama"))
                .andExpect(jsonPath("$.birthDate").value("01.01.2000"))
                .andExpect(jsonPath("$.address").value("Kyiv, 25"))
                .andExpect(jsonPath("$.phoneNumber").value("+1234567890"))
                .andReturn();


    }

    @Test
    void testUpdateUser_UserNotFound() throws Exception {
        //given
        when(userDao.findByEmail("test@gmail.com")).thenReturn(Optional.empty());
        //when then
        mvc.perform(MockMvcRequestBuilders
                        .put("/users/test@gmail.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(jsonPath("$.message").value("User with email test@gmail.com not found"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateUser_ValidationErrors() throws Exception {
        //given
        userDto.setLastName(null);
        doCallRealMethod().when(userValidator).validate(any(), any());
        User user = new User();
        when(userMapper.convertToUser(any(UserDto.class))).thenReturn(user);
        when(userDao.update(anyString(), any(User.class))).thenReturn(user);
        //when then
        mvc.perform(MockMvcRequestBuilders
                        .put("/users/test@gmail.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testDeleteUser_Success() throws Exception {

        User user = new User();

        when(userDao.findByEmail("test@gmail.com")).thenReturn(Optional.of(user));

        mvc.perform(MockMvcRequestBuilders
                        .delete("/users/test@gmail.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    void testDeleteUser_UserNotFound() throws Exception {
        User user = new User();

        when(userDao.findByEmail("test@gmail.com")).thenReturn(Optional.empty());

        mvc.perform(MockMvcRequestBuilders
                        .delete("/users/test@gmail.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(jsonPath("$.message").value("User with email test@gmail.com not found"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testSearchByDateRange_ValidRequest() throws Exception {

        List<User> users = Arrays.asList(new User(), new User());
        when(userDao.searchByBirthDayRange(dateForm.getFrom(), dateForm.getTo())).thenReturn(users);
        doCallRealMethod().when(dateValidator).validate(any(), any());
        mvc.perform(MockMvcRequestBuilders
                        .post("/users/search")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dateForm)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void testSearchByDateRange_InvalidDateForm() throws Exception {
        DateFormDto dateForm = new DateFormDto(LocalDate.now(), LocalDate.now().minusDays(1));
        doCallRealMethod().when(dateValidator).validate(any(), any());
        mvc.perform(MockMvcRequestBuilders
                        .post("/users/search")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dateForm)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("from - 'From' date should be before 'to' date;"));
    }

    @Test
    void testSearchByDateRange_ValidEmptyResult() throws Exception {
        DateFormDto dateForm = new DateFormDto(LocalDate.now().minusDays(1), LocalDate.now());
        when(userDao.searchByBirthDayRange(dateForm.getFrom(), dateForm.getTo())).thenReturn(Collections.emptyList());

        mvc.perform(MockMvcRequestBuilders
                        .post("/users/search")
                        .contentType("application/json")
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
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalidDateFormJson)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void patchUpdate_ExistingUserEmailPatched_ReturnsUpdatedUser() throws Exception {
        // given
        String email = "test@gmail.com";
        String updatedEmail = "new@gmail.com";
        User updatedUser = user;
        updatedUser.setEmail(updatedEmail);

        when(userDao.findByEmail(email)).thenReturn(Optional.of(user));
        when(userMapper.convertToUserDto(user)).thenReturn(userDto);
        when(userMapper.convertToUser(UPDATED_USER_DTO)).thenReturn(updatedUser);
        when(userDao.update(email, updatedUser)).thenReturn(updatedUser);
        when(userMapper.convertToUserDto(updatedUser)).thenReturn(UPDATED_USER_DTO);
        // when
        // then
        mvc.perform(MockMvcRequestBuilders
                        .patch("/users/{email}", email)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_NODE.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("new@gmail.com"));
    }

    @Test
    void patchUpdate_NonExistentUser_ReturnsNotFound() throws Exception {
        // given
        String email = "nonexistent@gmail.com";
        // when
        when(userDao.findByEmail(email)).thenReturn(Optional.empty());
        // then
        mvc.perform(MockMvcRequestBuilders
                        .patch("/users/{email}", email)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_NODE.toString()))
                .andExpect(jsonPath("$.message").value("User with email nonexistent@gmail.com not found"))
                .andExpect(status().isNotFound());
    }

    @Test
    void patchUpdate_NonExistentUser_ReturnsNotUpdated() throws Exception {
        // given
        String email = "user@example.com";

        when(userDao.findByEmail(email)).thenReturn(Optional.of(new User()));
        when(userMapper.convertToUserDto(any(User.class))).thenReturn(new UserDto());

        //when then
        mvc.perform(MockMvcRequestBuilders
                        .patch("/users/{email}", email)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_INCORRECT_NODE.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("User with email user@example.com not updated"));
    }


    private static UserDto createTestUserDto() {
        UserDto userDto = new UserDto();
        userDto.setEmail("test@gmail.com");
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

    private static UserDto createUpdatedUserDto() {
        UserDto userDto = new UserDto();
        userDto.setEmail("new@gmail.com");
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
