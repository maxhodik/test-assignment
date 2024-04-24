package ua.hodik.testTask;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.Validator;
import ua.hodik.testTask.util.DateValidator;
import ua.hodik.testTask.util.UserValidator;

@Configuration
public class TestConfiguration {


    @Bean(name = "userValidator")
    public Validator userValidator() {
        return new UserValidator();
    }

    @Bean(name = "dateValidator")
    public Validator dateValidator() {
        return new DateValidator();
    }
}
