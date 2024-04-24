package ua.hodik.testTask.dao;

import ua.hodik.testTask.model.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface UserDao {
    User create(User user);


    User update(String email, User user);

    List<User> findAll();

    Optional<User> findByEmail(String email);

    void delete(String email);

    List<User> searchByBirthDayRange(LocalDate from, LocalDate to);

}
