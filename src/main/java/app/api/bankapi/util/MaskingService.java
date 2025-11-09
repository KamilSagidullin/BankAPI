package app.api.bankapi.util;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MaskingService {
    private final PasswordEncoder passwordEncoder;

    public  String anonimizeCard(String cardNumber){
        String anonimizedCardNumber = "************" + cardNumber.substring(cardNumber.length() - 4,cardNumber.length());
        return anonimizedCardNumber;
    }
    public String hashCard(String cardNumber){
        return passwordEncoder.encode(cardNumber);
    }

}
