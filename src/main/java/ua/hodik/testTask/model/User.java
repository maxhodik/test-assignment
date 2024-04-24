package ua.hodik.testTask.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;


@Data
public class User {

    @Email
    @NotNull
    private String email;
    @NotNull
    private String FirstName;
    @NotNull
    private String LastName;
    @NotNull
    @Past
    @DateTimeFormat(pattern = "dd.MM.yyyy")
    private LocalDate birthDate;
    private String address;
    private String phoneNumber;
}

