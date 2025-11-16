package app.api.bankapi.controller;

import app.api.bankapi.config.JwtFilter;
import app.api.bankapi.dto.RegistrationUserDto;
import app.api.bankapi.service.AuthService;
import app.api.bankapi.util.JwtRequest;
import app.api.bankapi.util.JwtTokenUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false) // вырубаем security-фильтры
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    // ВАЖНО: замокаем фильтр, чтобы контекст не пытался создать реальный JwtFilter
    @MockBean
    private JwtFilter jwtFilter;

    // Дополнительно замокаем JwtTokenUtils (если где-то ещё потребуется)
    @MockBean
    private JwtTokenUtils jwtTokenUtils;

    // ---------- /api/v1/auth/create ----------

    @Test
    void createAuthToken_ok() throws Exception {
        when(authService.createAuthToken(any(JwtRequest.class)))
                .thenReturn((ResponseEntity) ResponseEntity.ok("TOKEN"));

        String json = """
                {
                  "fullName": "user",
                  "password": "pass"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().string("TOKEN"));

        verify(authService).createAuthToken(any(JwtRequest.class));
    }

    @Test
    void createAuthToken_unauthorized() throws Exception {
        when(authService.createAuthToken(any(JwtRequest.class)))
                .thenReturn((ResponseEntity) ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body("ERR"));

        String json = """
                {
                  "fullName": "user",
                  "password": "wrong"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("ERR"));
    }

    // ---------- /api/v1/auth/registration ----------

    @Test
    void registration_ok() throws Exception {
        when(authService.createNewUser(any(RegistrationUserDto.class)))
                .thenReturn((ResponseEntity) ResponseEntity.ok("TOKEN_REGISTER"));

        String json = """
                {
                  "fullName": "newUser",
                  "password": "123456",
                  "confirmPassword": "123456"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().string("TOKEN_REGISTER"));

        verify(authService).createNewUser(any(RegistrationUserDto.class));
    }

    @Test
    void registration_badRequest() throws Exception {
        when(authService.createNewUser(any(RegistrationUserDto.class)))
                .thenReturn((ResponseEntity) ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body("BAD"));

        String json = """
                {
                  "fullName": "newUser",
                  "password": "123456",
                  "confirmPassword": "000000"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("BAD"));
    }
}
