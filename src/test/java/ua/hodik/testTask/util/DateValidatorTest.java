package ua.hodik.testTask.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import ua.hodik.testTask.dto.DateFormDto;
import ua.hodik.testTask.exceptions.InvalidDataException;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class DateValidatorTest {

    @InjectMocks
    private DateValidator dateValidator;
    private DateFormDto dateFormDto;

    @BeforeEach
    void SetUp() {
        dateFormDto = new DateFormDto();
        dateFormDto.setFrom(LocalDate.now().minusDays(1));
        dateFormDto.setTo(LocalDate.now());
    }

    @Test
    void validate_ValidDate() {
        //given
        Errors errors = new BeanPropertyBindingResult(dateFormDto, "dateFormDto");
        //when
        dateValidator.validate(dateFormDto, errors);
        //then
        assertFalse(errors.hasErrors());
    }

    @Test
    void validate_InvalidDate_EqualDate() {
        //given
        dateFormDto.setFrom(LocalDate.now());
        Errors errors = new BeanPropertyBindingResult(dateFormDto, "dateFormDto");
        //when
        InvalidDataException exception = assertThrows(InvalidDataException.class, () -> dateValidator.validate(dateFormDto, errors));
        //then
        assertTrue(errors.hasErrors());
        assertEquals("from - 'From' date should be before 'to' date;", exception.getMessage());
    }

    @Test
    void validate_InvalidDate_FromIsAfterTo() {
        //given
        dateFormDto.setFrom(LocalDate.now().plusDays(1));
        Errors errors = new BeanPropertyBindingResult(dateFormDto, "dateFormDto");
        //when
        InvalidDataException exception = assertThrows(InvalidDataException.class, () -> dateValidator.validate(dateFormDto, errors));
        //then
        assertTrue(errors.hasErrors());
        assertEquals("from - 'From' date should be before 'to' date;", exception.getMessage());
    }
}