package ua.hodik.testTask.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ua.hodik.testTask.exceptions.*;

@ControllerAdvice
public class ExceptionHandlingController {

    @ExceptionHandler
    private ResponseEntity<ErrorResponse> exceptionHandler(UserAlreadyExistsException e) {
        ErrorResponse message = new ErrorResponse(e.getMessage());
        ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);

    }

    @ExceptionHandler
    private ResponseEntity<ErrorResponse> exceptionHandler(InvalidDataException e) {
        ErrorResponse message = new ErrorResponse(e.getMessage());
        ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);

    }

    @ExceptionHandler
    private ResponseEntity<ErrorResponse> exceptionHandler(UserNotCreatedException e) {
        ErrorResponse message = new ErrorResponse(e.getMessage());

        return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    private ResponseEntity<ErrorResponse> exceptionHandler(IllegalArgumentException e) {
        ErrorResponse message = new ErrorResponse(e.getMessage());
        return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    private ResponseEntity<ErrorResponse> exceptionHandler(UserNotFoundException e) {
        ErrorResponse message = new ErrorResponse(e.getMessage());
        return new ResponseEntity<>(message, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler
    private ResponseEntity<ErrorResponse> exceptionHandler(UserNotUpdatedException e) {
        ErrorResponse message = new ErrorResponse(e.getMessage());
        return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
    }

}
