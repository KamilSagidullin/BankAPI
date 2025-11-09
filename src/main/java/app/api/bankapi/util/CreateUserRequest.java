package app.api.bankapi.util;

import app.api.bankapi.entity.RoleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateUserRequest {
    @Pattern(regexp = "^[A-Z]+ [A-Z]+$\n",message = "Введите фамилию и имя заглавными буквами через пробел")
    @NotBlank(message = "Поле не должно быть пустым")
    private String fullName;

    @NotBlank(message = "Поле не должно быть пустым")
    private String password;

    @NotBlank(message = "Поле не должно быть пустым")
    private RoleType role;
}
