package app.api.bankapi.exception;

public class LackOfMoneyException extends RuntimeException{
    public LackOfMoneyException(String message) {
        super(message);
    }
}
