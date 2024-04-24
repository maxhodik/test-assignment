package ua.hodik.testTask.util;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import ua.hodik.testTask.dto.DateFormDto;

import java.time.LocalDate;

@Component
public class DateValidator implements AbstractValidator {
    @Override
    public boolean supports(Class<?> clazz) {
        return this.getClass().equals(clazz);
    }

    @Override
    public void validate(@NotNull Object target, Errors errors) {
        DateFormDto dateFormDto = (DateFormDto) target;
        LocalDate from = dateFormDto.getFrom();
        LocalDate to = dateFormDto.getTo();
        if(from.isAfter(to)){
            errors.rejectValue("from", "", "'From' date should be before 'to' date");
        }
        bindErrors((BindingResult) errors);

    }
}
