package app.api.bankapi.util;

import lombok.Data;

import java.io.Serializable;

@Data
public class JwtRequest implements Serializable {
    private String fullName;
    private String password;
}
