package app.api.bankapi.controller;

import app.api.bankapi.entity.Card;
import app.api.bankapi.repository.specification.CardFilter;
import app.api.bankapi.security.MyUserDetails;
import app.api.bankapi.service.CardService;
import app.api.bankapi.service.MaskingService;
import app.api.bankapi.util.JwtTokenUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class CardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // глушим JWT, чтобы не требовались реальные токены/секрет

    @MockBean
    private JwtTokenUtils jwtTokenUtils;

    @MockBean
    private CardService cardService;

    @MockBean
    private MaskingService maskingService;

    @Container
    private static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:latest"))
                    .withReuse(true)
                    .withUsername("postgres")
                    .withPassword("postgres")
                    .withDatabaseName("bank_db");

    @DynamicPropertySource
    static void registerPgProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    // ==== helper для user-эндпоинтов с MyUserDetails ====

    private Authentication authWithRole(String role) {
        MyUserDetails principal = Mockito.mock(MyUserDetails.class);
        Mockito.when(principal.getId()).thenReturn(1L);

        return new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(new SimpleGrantedAuthority(role))
        );
    }

    // ======================================================================
    //                          ADMIN ENDPOINTS
    // ======================================================================

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void createCard_asAdmin_ok() throws Exception {
        Card card = Card.builder()
                .id(1L)
                .cardNumberLast("3456")
                .cardNumberCrypt("encrypted")
                .expiringAt(LocalDate.now().plusYears(2))
                .balance(1000L)
                .build();

        Mockito.when(cardService.saveCard(any())).thenReturn(card);

        String json = """
            {
              "cardNumber": "1234567890123456",
              "cardOwner": { "id": 1 },
              "expiringDate": "20/12/2030"
            }
            """;

        mockMvc.perform(post("/api/v1/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void createCard_asUser_forbidden() throws Exception {
        String json = """
            {
              "cardNumber": "1234567890123456",
              "cardOwner": { "id": 1 },
              "expiringDate": "20/12/2030"
            }
            """;

        mockMvc.perform(post("/api/v1/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithAnonymousUser
    void createCard_anonymous_forbidden() throws Exception {
        String json = """
            {
              "cardNumber": "1234567890123456",
              "cardOwner": { "id": 1 },
              "expiringDate": "20/12/2030"
            }
            """;

        mockMvc.perform(post("/api/v1/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void activateCard_asAdmin_ok() throws Exception {
        mockMvc.perform(post("/api/v1/cards/activate/{id}", 1L))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void activateCard_asUser_forbidden() throws Exception {
        mockMvc.perform(post("/api/v1/cards/activate/{id}", 1L))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithAnonymousUser
    void activateCard_anonymous_forbidden() throws Exception {
        mockMvc.perform(post("/api/v1/cards/activate/{id}", 1L))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void blockCard_asAdmin_ok() throws Exception {
        mockMvc.perform(post("/api/v1/cards/block/{id}", 1L))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void blockCard_asUser_forbidden() throws Exception {
        mockMvc.perform(post("/api/v1/cards/block/{id}", 1L))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithAnonymousUser
    void blockCard_anonymous_forbidden() throws Exception {
        mockMvc.perform(post("/api/v1/cards/block/{id}", 1L))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void deleteCard_asAdmin_ok() throws Exception {
        mockMvc.perform(post("/api/v1/cards/delete/{id}", 1L))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void deleteCard_asUser_forbidden() throws Exception {
        mockMvc.perform(post("/api/v1/cards/delete/{id}", 1L))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithAnonymousUser
    void deleteCard_anonymous_forbidden() throws Exception {
        mockMvc.perform(post("/api/v1/cards/delete/{id}", 1L))
                .andExpect(status().isForbidden());
    }

    // ======================================================================
    //                   USER ENDPOINTS (@AuthenticationPrincipal)
    //      (здесь нужен MyUserDetails, поэтому используем authentication())
    // ======================================================================

    @Test
    void getAllUserCards_asUser_ok() throws Exception {
        Authentication auth = authWithRole("ROLE_USER");

        Mockito.when(cardService.findByOwnerId(1L))
                .thenReturn(List.of(Card.builder().id(1L).build()));

        mockMvc.perform(get("/api/v1/cards/getMyCards")
                        .with(authentication(auth)))
                .andExpect(status().isOk());
    }

    @Test
    void getAllUserCards_asAdmin_forbidden() throws Exception {
        Authentication auth = authWithRole("ROLE_ADMIN");

        mockMvc.perform(get("/api/v1/cards/getMyCards")
                        .with(authentication(auth)))
                .andExpect(status().isForbidden());
    }

    @Test
    void askForBlockCard_asUser_ok() throws Exception {
        Authentication auth = authWithRole("ROLE_USER");

        Mockito.doNothing()
                .when(cardService)
                .blockCardByOwnerRequest(eq(1L), eq(1L));

        mockMvc.perform(post("/api/v1/cards/askForBlockCard/{id}", 1L)
                        .with(authentication(auth)))
                .andExpect(status().isNoContent());
    }

    @Test
    void askForBlockCard_asAdmin_forbidden() throws Exception {
        Authentication auth = authWithRole("ROLE_ADMIN");

        mockMvc.perform(post("/api/v1/cards/askForBlockCard/{id}", 1L)
                        .with(authentication(auth)))
                .andExpect(status().isForbidden());
    }

    @Test
    void sendMoney_asUser_ok() throws Exception {
        Authentication auth = authWithRole("ROLE_USER");

        String json = """
            {
              "cardSenderId": 1,
              "cardReceiverId": 2,
              "moneyToSend": 100
            }
            """;

        mockMvc.perform(post("/api/v1/cards/sendMoney")
                        .with(authentication(auth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        Mockito.verify(cardService).sendMoney(
                any(MyUserDetails.class),
                eq(1L),
                eq(2L),
                eq(100L)
        );
    }

    @Test
    void sendMoney_asAdmin_forbidden() throws Exception {
        Authentication auth = authWithRole("ROLE_ADMIN");

        String json = """
            {
              "cardSenderId": 1,
              "cardReceiverId": 2,
              "moneyToSend": 100
            }
            """;

        mockMvc.perform(post("/api/v1/cards/sendMoney")
                        .with(authentication(auth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isForbidden());
    }

    @Test
    void checkBalance_asUser_ok() throws Exception {
        Authentication auth = authWithRole("ROLE_USER");

        Mockito.when(cardService.checkBalance(any(MyUserDetails.class), eq(1L)))
                .thenReturn(1000L);

        mockMvc.perform(get("/api/v1/cards/balance/{id}", 1L)
                        .with(authentication(auth)))
                .andExpect(status().isOk());
    }

    @Test
    void checkBalance_asAdmin_forbidden() throws Exception {
        Authentication auth = authWithRole("ROLE_ADMIN");

        mockMvc.perform(get("/api/v1/cards/balance/{id}", 1L)
                        .with(authentication(auth)))
                .andExpect(status().isForbidden());
    }

    // ======================================================================
    //                           FILTER ENDPOINT
    // ======================================================================

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void filter_asUser_ok() throws Exception {
        Page<Card> page = new PageImpl<>(
                List.of(Card.builder().id(1L).build()),
                PageRequest.of(0, 20),
                1
        );

        Mockito.when(cardService.filter(any(CardFilter.class), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/cards/filter"))
                .andExpect(status().isOk());
    }

    @Test
    @WithAnonymousUser
    void filter_anonymous_forbidden() throws Exception {
        mockMvc.perform(get("/api/v1/cards/filter"))
                .andExpect(status().isForbidden());
    }
    // ===================== USER ENDPOINTS: ANONYMOUS =====================

    @Test
    @WithAnonymousUser
    void getAllUserCards_anonymous_forbidden() throws Exception {
        mockMvc.perform(get("/api/v1/cards/getMyCards"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithAnonymousUser
    void askForBlockCard_anonymous_forbidden() throws Exception {
        mockMvc.perform(post("/api/v1/cards/askForBlockCard/{id}", 1L))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithAnonymousUser
    void sendMoney_anonymous_forbidden() throws Exception {
        String json = """
            {
              "cardSenderId": 1,
              "cardReceiverId": 2,
              "moneyToSend": 100
            }
            """;

        mockMvc.perform(post("/api/v1/cards/sendMoney")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithAnonymousUser
    void checkBalance_anonymous_forbidden() throws Exception {
        mockMvc.perform(get("/api/v1/cards/balance/{id}", 1L))
                .andExpect(status().isForbidden());
    }
    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void createCard_asAdmin_invalidCardNumber_badRequest() throws Exception {
        // номер карты слишком короткий, ожидаем падение валидации
        String json = """
            {
              "cardNumber": "12345678",
              "cardOwner": { "id": 1 },
              "expiringDate": "20/12/2030"
            }
            """;

        mockMvc.perform(post("/api/v1/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        // сервис сохранения карты не должен вызываться при невалидном запросе
        Mockito.verify(cardService, Mockito.never()).saveCard(any());
    }
    @Test
    void sendMoney_asUser_negativeAmount_badRequest() throws Exception {
        Authentication auth = authWithRole("ROLE_USER");

        String json = """
            {
              "cardSenderId": 1,
              "cardReceiverId": 2,
              "moneyToSend": -100
            }
            """;

        mockMvc.perform(post("/api/v1/cards/sendMoney")
                        .with(authentication(auth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        // сервис не должен вызываться с такой суммой, если стоит валидация на DTO
        Mockito.verify(cardService, Mockito.never())
                .sendMoney(any(MyUserDetails.class), anyLong(), anyLong(), anyLong());
    }

}
