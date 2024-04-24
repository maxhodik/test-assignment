package ua.hodik.testTask.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import ua.hodik.testTask.TestConfiguration;
import ua.hodik.testTask.dto.UserDto;
import ua.hodik.testTask.exceptions.InvalidDataException;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserValidatorTest {

    @Mock
    private Validator validator;

    @InjectMocks
    private UserValidator userValidator;

    private UserDto userDto;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(userValidator, "minAge", 18);
        userDto = new UserDto();
        userDto.setBirthDate(LocalDate.now().minusYears(20));
    }

    @Test
    void supports() {
        assertTrue(userValidator.supports(UserValidator.class));
        assertFalse(userValidator.supports(String.class));
    }

    @Test
    void validate_ValidUserDto() {
        //given
        Errors errors = new BeanPropertyBindingResult(userDto, "userDto");
        //when
        userValidator.validate(userDto, errors);
        //then
        assertFalse(errors.hasErrors());
        verify(validator, times(1)).validate(eq(userDto), any(Errors.class));
    }

    @Test
    void validate_TooYoungUserDto() {
        //given
        userDto.setBirthDate(LocalDate.now().minusYears(10));
        Errors errors = new BeanPropertyBindingResult(userDto, "userDto");
        //when
        InvalidDataException exception = assertThrows(InvalidDataException.class, () -> userValidator.validate(userDto, errors));
        //then
        assertTrue(errors.hasErrors());
        assertEquals("birthDate - You are too young!!!;", exception.getMessage());
        verify(validator, times(1)).validate(eq(userDto), any(Errors.class));
    }
}