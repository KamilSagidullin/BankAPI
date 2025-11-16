package app.api.bankapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class RegistrationUserDto {
    @Pattern(regexp = "^[A-Z]+ [A-Z]+$\n",message = "Введите фамилию и имя заглавными буквами через пробел")
    @NotBlank(message = "Поле не должно быть пустым")
    private String fullName;

    @NotBlank(message = "Поле не должно быть пустым")
    private String password;

    @NotBlank(message = "Поле не должно быть пустым")
    private String confirmPassword;
}
