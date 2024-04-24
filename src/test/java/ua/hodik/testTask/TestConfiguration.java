package ua.hodik.testTask;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ua.hodik.testTask.util.AbstractValidator;
import ua.hodik.testTask.util.DateValidator;
import ua.hodik.testTask.util.UserValidator;

@Configuration
public class TestConfiguration {
    @Bean(name = "userValidator")
    public AbstractValidator userValidator() {
        return new UserValidator();
    }

    @Bean(name = "dateValidator")
    public AbstractValidator dateValidator() {
        return new DateValidator();
    }

}
