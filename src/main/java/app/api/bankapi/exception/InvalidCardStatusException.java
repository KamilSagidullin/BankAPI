package app.api.bankapi.exception;

public class InvalidCardStatusException extends RuntimeException{
    public InvalidCardStatusException(String message) {
        super(message);
    }
}
