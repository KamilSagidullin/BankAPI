package app.api.bankapi.controller;

import app.api.bankapi.entity.Card;
import app.api.bankapi.service.CardService;
import app.api.bankapi.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Container
    private static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:latest"))
                    .withUsername("postgres")
                    .withPassword("postgres")
                    .withDatabaseName("bank_db");

    // === ВАЖНО: настраиваем DataSource и jwt.secret так же, как в CardControllerTest ===
    @DynamicPropertySource
    static void registerPgProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        // если у тебя уже есть jwt.secret в CardControllerTest – сделай одинаково
        registry.add("jwt.secret", () -> "test_jwt_secret_key");
    }

    @MockBean
    private UserService userService;

    @MockBean
    private CardService cardService;

    // ---------- deleteUser ----------

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void deleteUser_asAdmin_ok() throws Exception {
        Long userId = 1L;

        mockMvc.perform(post("/api/v1/admin/deleteUser/{id}", userId))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(eq(userId));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void deleteUser_asUser_forbidden() throws Exception {
        Long userId = 1L;

        mockMvc.perform(post("/api/v1/admin/deleteUser/{id}", userId))
                .andExpect(status().isForbidden());

        verifyNoInteractions(userService);
    }

    @Test
    @WithAnonymousUser
    void deleteUser_anonymous_forbidden() throws Exception {
        Long userId = 1L;

        mockMvc.perform(post("/api/v1/admin/deleteUser/{id}", userId))
                .andExpect(status().isForbidden());

        verifyNoInteractions(userService);
    }

    // ---------- getAllCards ----------

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void getAllCards_asAdmin_ok() throws Exception {
        Card card1 = Card.builder()
                .id(1L)
                .cardNumberLast("1111")
                .cardNumberCrypt("enc-1111")
                .expiringAt(LocalDate.of(2030, 12, 31))
                .balance(1000L)
                .build();

        Card card2 = Card.builder()
                .id(2L)
                .cardNumberLast("2222")
                .cardNumberCrypt("enc-2222")
                .expiringAt(LocalDate.of(2031, 1, 31))
                .balance(2000L)
                .build();

        when(cardService.findAll()).thenReturn(List.of(card1, card2));

        mockMvc.perform(get("/api/v1/admin/allCards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        verify(cardService).findAll();
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void getAllCards_asUser_forbidden() throws Exception {
        mockMvc.perform(get("/api/v1/admin/allCards"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(cardService);
    }

    @Test
    @WithAnonymousUser
    void getAllCards_anonymous_forbidden() throws Exception {
        mockMvc.perform(get("/api/v1/admin/allCards"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(cardService);
    }
}
