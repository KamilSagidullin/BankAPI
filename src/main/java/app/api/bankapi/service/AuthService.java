package app.api.bankapi.service;

import app.api.bankapi.dto.RegistrationUserDto;
import app.api.bankapi.entity.RoleType;
import app.api.bankapi.entity.User;
import app.api.bankapi.exception.AppError;
import app.api.bankapi.security.MyUserDetails;
import app.api.bankapi.security.UserDetailsImpl;
import app.api.bankapi.util.JwtRequest;
import app.api.bankapi.util.JwtResponse;
import app.api.bankapi.util.JwtTokenUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserDetailsImpl userDetailsService;
    private final JwtTokenUtils jwtTokenUtils;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;

    public ResponseEntity<?> createAuthToken(@RequestBody JwtRequest authRequest){
        System.out.println(passwordEncoder.getClass());
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authRequest.getFullName(), authRequest.getPassword()));
        } catch (BadCredentialsException ex){
            return new ResponseEntity<>(new AppError(HttpStatus.UNAUTHORIZED.value(),"Неккоректный логин или пароль"),HttpStatus.UNAUTHORIZED);
        }
        UserDetails userDetails =  userDetailsService.loadUserByUsername(authRequest.getFullName());
        String token = jwtTokenUtils.generateToken(userDetails);
        return ResponseEntity.ok(new JwtResponse(token));

    }
    public ResponseEntity<?> createNewUser (@RequestBody RegistrationUserDto registrationUserDto){
        log.info("Создание нового пользователя {}", registrationUserDto);
        if (!registrationUserDto.getConfirmPassword().equals(registrationUserDto.getPassword())){
            return new ResponseEntity<>(new AppError(HttpStatus.BAD_REQUEST.value(), "Пароли не совпадают"),HttpStatus.BAD_REQUEST);
        }

        if (userService.findByFullName(registrationUserDto.getFullName()).isPresent()){
            return  new ResponseEntity<>(new AppError(HttpStatus.BAD_REQUEST.value(), "Пользователь с таким именем уже существует"),HttpStatus.BAD_REQUEST);
        }
        User user = new User();
        user.setFullName(registrationUserDto.getFullName());
        user.setRoles(Set.of(RoleType.ROLE_USER));
        user.setPassword(passwordEncoder.encode(registrationUserDto.getPassword()));
        userService.save(user);

        log.info("Новый пользователь создан {}", user);

        return ResponseEntity.ok(jwtTokenUtils.generateToken(new MyUserDetails(user)));


    }
}

