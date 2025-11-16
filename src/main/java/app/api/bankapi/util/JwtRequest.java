package app.api.bankapi.util;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@Data
public class JwtRequest implements Serializable {
    @NotBlank(message = "Поле не должно быть пустым")
    private String fullName;

    @NotBlank(message = "Поле не должно быть пустым")
    private String password;
}
