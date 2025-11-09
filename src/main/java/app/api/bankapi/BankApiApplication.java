package app.api.bankapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class BankApiApplication {

    public static void main(String[] args) {
        System.out.println(new BCryptPasswordEncoder(12).encode("12345"));

        SpringApplication.run(BankApiApplication.class, args);
    }

}
