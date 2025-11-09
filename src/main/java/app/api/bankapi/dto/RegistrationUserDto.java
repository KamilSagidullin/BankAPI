package app.api.bankapi.dto;

import lombok.Data;

@Data
public class RegistrationUserDto {
    private String fullName;
    private String password;
    private String confirmPassword;
}
