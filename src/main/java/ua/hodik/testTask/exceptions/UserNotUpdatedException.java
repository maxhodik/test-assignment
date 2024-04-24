package ua.hodik.testTask.exceptions;

public class UserNotUpdatedException extends RuntimeException {
    public UserNotUpdatedException(String message, Exception e) {
        super(message, e);
    }

    public UserNotUpdatedException(String message) {
        super(message);

    }
}
