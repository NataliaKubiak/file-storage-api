package org.example.filestorageapi.errors;

import jakarta.validation.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({
            ValidationException.class
    })
    public ResponseEntity<ErrorResponse> handleValidationException(Exception ex) {
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.BAD_REQUEST); //400
    }

    @ExceptionHandler({
            UsernameNotFoundException.class,
            AuthenticationException.class
    })
    public ResponseEntity<ErrorResponse> handleAuthenticationException(Exception ex) {
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.UNAUTHORIZED); //401
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
