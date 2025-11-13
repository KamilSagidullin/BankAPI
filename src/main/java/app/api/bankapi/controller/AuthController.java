package app.api.bankapi.controller;

import app.api.bankapi.dto.RegistrationUserDto;
import app.api.bankapi.service.AuthService;
import app.api.bankapi.util.JwtRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
   private final AuthService authService;

    @PostMapping("/create")
    public ResponseEntity<?> createAuthToken(@RequestBody JwtRequest authRequest){
    return authService.createAuthToken(authRequest);
    }
    @PostMapping("/registration")
    public ResponseEntity<?> createNewUser (@RequestBody RegistrationUserDto registrationUserDto){
        return authService.createNewUser(registrationUserDto);
    }
}
