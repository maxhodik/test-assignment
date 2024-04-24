package ua.hodik.testTask.util;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import ua.hodik.testTask.dto.UserDto;

import java.time.LocalDate;

@Component("userValidator")
public class UserValidator implements Validator {
    @Value("${minAge}")
    private int minAge;

    @Autowired
    private Validator validator;

    @Override
    public boolean supports(Class<?> clazz) {
        return this.getClass().equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        validator.validate(target, errors);
        UserDto userDto = (UserDto) target;
        LocalDate birthDate = userDto.getBirthDate();
        if (birthDate.plusYears(minAge).isAfter(LocalDate.now())) {
            errors.rejectValue("birthDate", "", "You are too young!!!");
        }
    }
}
