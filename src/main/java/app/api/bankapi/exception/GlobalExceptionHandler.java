package app.api.bankapi.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Date;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(CardNotFoundException.class)
    public ResponseEntity<AppError> cardNotFoundException(CardNotFoundException e){
        log.error("Card not found exception", e);
        return response(HttpStatus.NOT_FOUND, e.getMessage());
    }
    @ExceptionHandler(InvalidCardStatusException.class)
    public ResponseEntity<AppError> invalidCardStatusException(InvalidCardStatusException e){
        log.error("Invalid card status", e);
        return response(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(LackOfMoneyException.class)
    public ResponseEntity<AppError> lackOfMoneyException(LackOfMoneyException e){
        log.error("Lack of money", e);
        return response(HttpStatus.PAYMENT_REQUIRED, e.getMessage());

    }
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<AppError> userNotFoundException(UserNotFoundException e){
        log.error("User not found exception", e);
        return response(HttpStatus.NOT_FOUND, e.getMessage());
    }

    private ResponseEntity<AppError> response(HttpStatus status, String message){
        var response = AppError.builder().status(status.value()).message(message).timestamp(new Date()).build();
        return ResponseEntity.status(status).body(response);

    }
}