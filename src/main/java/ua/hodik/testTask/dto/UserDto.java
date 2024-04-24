package ua.hodik.testTask.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.Builder;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class UserDto {

    private long id;
    @NotBlank(message = "Should not be empty")
    @Email
    private String email;
    @NotBlank(message = "Should not be empty")
    private String firstName;
    @NotBlank(message = "Should not be empty")
    private String lastName;
    @Past(message = "date has to be in past")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(pattern = "dd.MM.yyyy")
    @NotNull(message = "Should not be empty")
    private LocalDate birthDate;
    private String address;
    private String phoneNumber;

}