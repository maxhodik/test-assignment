package ua.hodik.testTask.dao;

import org.springframework.stereotype.Component;
import ua.hodik.testTask.model.User;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class UserDaoImpl implements UserDao {
    private Map<String, User> userMap = new HashMap();

    @Override
    public User create(User user) {
        userMap.put(user.getEmail(), user);
        return user;
    }


    @Override
    public User update(String email, User user) {
         userMap.put(email, user);
         return user;
    }

    @Override
    public List<User> findAll() {
        return userMap.values().stream().toList();
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return Optional.ofNullable(userMap.get(email));
    }

    @Override
    public void delete(String email) {
        userMap.remove(email);
    }

    @Override
    public List<User> searchByBirthDayRange(LocalDate from, LocalDate to) {
        return userMap.values().stream()
                .filter(user -> user.getBirthDate().isAfter(from) && user.getBirthDate().isBefore(to))
                .collect(Collectors.toList());
    }
}
