import app.api.bankapi.dto.RegistrationUserDto;
import app.api.bankapi.entity.User;
import app.api.bankapi.exception.AppError;
import app.api.bankapi.security.MyUserDetails;
import app.api.bankapi.security.UserDetailsImpl;
import app.api.bankapi.service.AuthService;
import app.api.bankapi.service.UserService;
import app.api.bankapi.util.JwtRequest;
import app.api.bankapi.util.JwtResponse;
import app.api.bankapi.util.JwtTokenUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserDetailsImpl userDetailsService;

    @Mock
    private JwtTokenUtils jwtTokenUtils;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthService authService;

    // ---------- createAuthToken ----------

    @Test
    void createAuthToken_success() {
        JwtRequest request = new JwtRequest();
        request.setFullName("user");
        request.setPassword("pass");

        UserDetails userDetails = mock(UserDetails.class);
        when(userDetailsService.loadUserByUsername("user")).thenReturn(userDetails);
        when(jwtTokenUtils.generateToken(userDetails)).thenReturn("TOKEN");

        ResponseEntity<?> response = authService.createAuthToken(request);

        // authenticationManager.authenticate должен вызваться
        verify(authenticationManager).authenticate(
                any(UsernamePasswordAuthenticationToken.class)
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertInstanceOf(JwtResponse.class, response.getBody());
        JwtResponse body = (JwtResponse) response.getBody();
        assertEquals("TOKEN", body.getToken());
    }

    @Test
    void createAuthToken_badCredentials_returnsUnauthorized() {
        JwtRequest request = new JwtRequest();
        request.setFullName("user");
        request.setPassword("wrong");

        // эмулируем неверный логин/пароль
        doThrow(new BadCredentialsException("bad creds"))
                .when(authenticationManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class));

        ResponseEntity<?> response = authService.createAuthToken(request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertInstanceOf(AppError.class, response.getBody());
        AppError error = (AppError) response.getBody();
        assertEquals(HttpStatus.UNAUTHORIZED.value(), error.getStatus());
        assertEquals("Неккоректный логин или пароль", error.getMessage());

        // при BadCredentials токен не генерируется
        verifyNoInteractions(userDetailsService, jwtTokenUtils);
    }

    // ---------- createNewUser ----------

    @Test
    void createNewUser_passwordMismatch_returnsBadRequest() {
        RegistrationUserDto dto = new RegistrationUserDto();
        dto.setFullName("user");
        dto.setPassword("123456");
        dto.setConfirmPassword("000000");

        ResponseEntity<?> response = authService.createNewUser(dto);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertInstanceOf(AppError.class, response.getBody());
        AppError error = (AppError) response.getBody();
        assertEquals(HttpStatus.BAD_REQUEST.value(), error.getStatus());
        assertEquals("Пароли не совпадают", error.getMessage());

        verifyNoInteractions(userService, passwordEncoder, jwtTokenUtils);
    }

    @Test
    void createNewUser_userAlreadyExists_returnsBadRequest() {
        RegistrationUserDto dto = new RegistrationUserDto();
        dto.setFullName("user");
        dto.setPassword("123456");
        dto.setConfirmPassword("123456");

        when(userService.findByFullName("user"))
                .thenReturn(Optional.of(new User()));

        ResponseEntity<?> response = authService.createNewUser(dto);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertInstanceOf(AppError.class, response.getBody());
        AppError error = (AppError) response.getBody();
        assertEquals(HttpStatus.BAD_REQUEST.value(), error.getStatus());
        assertEquals("Пользователь с таким именем уже существует", error.getMessage());

        verify(userService).findByFullName("user");
        verifyNoMoreInteractions(userService);
        verifyNoInteractions(passwordEncoder, jwtTokenUtils);
    }

    @Test
    void createNewUser_success() {
        RegistrationUserDto dto = new RegistrationUserDto();
        dto.setFullName("user");
        dto.setPassword("123456");
        dto.setConfirmPassword("123456");

        when(userService.findByFullName("user")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("123456")).thenReturn("ENC_PWD");
        when(jwtTokenUtils.generateToken(any(MyUserDetails.class)))
                .thenReturn("NEW_TOKEN");

        ResponseEntity<?> response = authService.createNewUser(dto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("NEW_TOKEN", response.getBody());

        // проверяем, что:
        // 1) искали пользователя
        verify(userService).findByFullName("user");
        // 2) заэнкодили пароль
        verify(passwordEncoder).encode("123456");
        // 3) сохранили нового пользователя
        verify(userService).save(any(User.class));
        // 4) сгенерировали токен
        verify(jwtTokenUtils).generateToken(any(MyUserDetails.class));
    }
}
