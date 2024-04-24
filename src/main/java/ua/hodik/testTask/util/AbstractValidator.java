package ua.hodik.testTask.util;

import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.Validator;
import ua.hodik.testTask.exceptions.InvalidDataException;

import java.util.List;

public interface AbstractValidator extends Validator {
    default void bindErrors(BindingResult bindingResult) {
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
