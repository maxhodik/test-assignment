package ua.hodik.testTask.exceptions;

public class UserNotCreatedException extends RuntimeException {
    public UserNotCreatedException() {
    }

    public UserNotCreatedException(String message) {
        super(message);
    }
}
