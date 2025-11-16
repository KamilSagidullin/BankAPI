package app.api.bankapi.integration;

import app.api.bankapi.BankApiApplication;
import app.api.bankapi.entity.RoleType;
import app.api.bankapi.entity.User;
import app.api.bankapi.util.JwtResponse;
import app.api.bankapi.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = BankApiApplication.class)
@AutoConfigureMockMvc
@Testcontainers
@ExtendWith(SpringExtension.class)
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper; // для распарсивания JwtResponse

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Container
    private static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:latest"))
                    .withDatabaseName("bank_db")
                    .withUsername("postgres")
                    .withPassword("postgres");

    @DynamicPropertySource
    static void configureDataSource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        // Если в application.yml ливкбейз включен по умолчанию — этого достаточно.
        // Если ты его отключал в тестах, можно тут явно включить:
        // registry.add("spring.liquibase.enabled", () -> "true");
    }

    // ====== Вспомогательные методы ======

    /**
     * Регистрация нового обычного пользователя и получение JWT-токена,
     * который возвращается из /api/v1/auth/registration как обычная строка.
     */
    private String registerUserAndGetToken(String fullName, String password) throws Exception {
        String json = """
            {
              "fullName": "%s",
              "password": "%s",
              "confirmPassword": "%s"
            }
            """.formatted(fullName, password, password);

        var result = mockMvc.perform(post("/api/v1/auth/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andReturn();

        // createNewUser в AuthService возвращает ResponseEntity.ok(jwtTokenUtils.generateToken(...))
        // т.е. в body просто строка с токеном
        return result.getResponse().getContentAsString();
    }

    /**
     * Создаём ADMIN-пользователя напрямую через UserService и получаем JWT-токен
     * через эндпоинт /api/v1/auth/create (который возвращает JwtResponse{token}).
     */
    private String createAdminAndGetToken(String fullName, String rawPassword) throws Exception {
        // 1. создаём admin в БД
        User admin = new User(fullName, passwordEncoder.encode(rawPassword), RoleType.ROLE_ADMIN);
        userService.save(admin);

        // 2. логинимся через /auth/create и получаем JwtResponse
        String loginJson = """
            {
              "fullName": "%s",
              "password": "%s"
            }
            """.formatted(fullName, rawPassword);

        var result = mockMvc.perform(post("/api/v1/auth/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        JwtResponse jwt = objectMapper.readValue(body, JwtResponse.class);
        return jwt.getToken();
    }

    // ====== ТЕСТЫ БЕЗОПАСНОСТИ ======

    /**
     * Аноним не должен иметь доступ к пользовательскому эндпоинту.
     * Здесь я ожидаю любой 4xx (401 или 403 — зависит от твоего SecurityConfig).
     */
    @Test
    void anonymous_cannot_access_user_cards() throws Exception {
        mockMvc.perform(get("/api/v1/cards/getMyCards"))
                .andExpect(status().is4xxClientError());
    }

    /**
     * Обычный пользователь (ROLE_USER), получив JWT при регистрации,
     * может обращаться к /api/v1/cards/getMyCards.
     */
    @Test
    void user_with_jwt_can_access_user_cards() throws Exception {
        String username = "user_" + UUID.randomUUID();
        String password = "123456";

        String token = registerUserAndGetToken(username, password);

        mockMvc.perform(get("/api/v1/cards/getMyCards")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk());
    }

    /**
     * Обычный пользователь (ROLE_USER) с валидным токеном
     * НЕ может попасть в /api/v1/admin/allCards.
     */
    @Test
    void user_with_jwt_cannot_access_admin_endpoints() throws Exception {
        String username = "user_" + UUID.randomUUID();
        String password = "123456";

        String token = registerUserAndGetToken(username, password);

        mockMvc.perform(get("/api/v1/admin/allCards")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden()); // если будет 401 — поменяй на is4xxClientError()
    }

    /**
     * Админ (ROLE_ADMIN) с JWT должен иметь доступ к /api/v1/admin/allCards.
     */
    @Test
    void admin_with_jwt_can_access_admin_endpoints() throws Exception {
        String username = "admin_" + UUID.randomUUID();
        String password = "123456";

        String token = createAdminAndGetToken(username, password);

        mockMvc.perform(get("/api/v1/admin/allCards")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk());
    }

    /**
     * Регистрация должна быть доступна без авторизации (public endpoint).
     */
    @Test
    void registration_is_public() throws Exception {
        String username = "public_" + UUID.randomUUID();
        String password = "123456";

        String json = """
            {
              "fullName": "%s",
              "password": "%s",
              "confirmPassword": "%s"
            }
            """.formatted(username, password, password);

        mockMvc.perform(post("/api/v1/auth/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());
    }
}
