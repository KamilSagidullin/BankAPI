package app.api.bankapi.exception;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class AppError {
    private int status;
    private String message;
    private Date timestamp;

    public AppError(int status, String message) {
        this.status = status;
        this.message = message;
        this.timestamp = new Date();
    }
}
