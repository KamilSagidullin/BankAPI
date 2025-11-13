package app.api.bankapi.util;

import app.api.bankapi.entity.User;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
@Getter
public class CreateCardRequest {
    @Size(min = 16,max = 16)
    private String cardNumber;
    private User cardOwner;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate expiringDate;

}
