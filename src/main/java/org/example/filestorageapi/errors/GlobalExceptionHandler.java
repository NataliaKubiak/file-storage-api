package org.example.filestorageapi.errors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({
            IllegalArgumentException.class
    })
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(Exception ex) {
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.BAD_REQUEST); //400
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUsernameNotFoundException(Exception ex) {
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.UNAUTHORIZED); //401
    }

    @ExceptionHandler({
            AuthenticationException.class
    })
    public ResponseEntity<ErrorResponse> handleAuthenticationException() {
        return new ResponseEntity<>(new ErrorResponse("Incorrect login or password"), HttpStatus.UNAUTHORIZED); //401
    }

    @ExceptionHandler({
            UserAlreadyExistException.class
    })
    public ResponseEntity<ErrorResponse> handleUserAlreadyExistException(Exception ex) {
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.CONFLICT); //409
    }

    @ExceptionHandler({
            Exception.class
    })
    public ResponseEntity<ErrorResponse> handleUnexpectedException(Exception ex) {
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR); //500
    }
}
