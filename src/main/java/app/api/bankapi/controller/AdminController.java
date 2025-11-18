package app.api.bankapi.controller;

import app.api.bankapi.entity.Card;
import app.api.bankapi.service.CardService;
import app.api.bankapi.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@PreAuthorize("hasRole('ROLE_ADMIN')")
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {
    private final UserService userService;
    private final CardService cardService;

    @PostMapping("/deleteUser/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable("id") Long id){
        log.info("Deleting user {}", id);
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/allCards")
       public ResponseEntity<List<Card>> getAllCards(){
        log.info("Receiving all cards all cards");
        return ResponseEntity.ok(cardService.findAll());

    }

}
