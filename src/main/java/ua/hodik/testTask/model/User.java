package ua.hodik.testTask.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;


@Data
public class User {

    private long id;
    private String email;

    private String FirstName;

    private String LastName;


    private LocalDate birthDate;
    private String address;
    private String phoneNumber;
}

