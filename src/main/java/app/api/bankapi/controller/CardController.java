package app.api.bankapi.controller;

import app.api.bankapi.dto.CardDto;
import app.api.bankapi.dto.PageResponse;
import app.api.bankapi.entity.Card;
import app.api.bankapi.repository.specification.CardFilter;
import app.api.bankapi.security.MyUserDetails;
import app.api.bankapi.service.CardService;
import app.api.bankapi.util.CreateCardRequest;
import app.api.bankapi.service.MaskingService;
import app.api.bankapi.util.SendMoneyRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static java.util.stream.Collectors.partitioningBy;

@RestController
@RequestMapping("/api/v1/cards")
@RequiredArgsConstructor
@Slf4j
public class CardController {
    private final MaskingService maskingService;
    private final CardService cardService;
    private final ModelMapper modelMapper;
    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<CardDto> createCard(@RequestBody CreateCardRequest createCardRequest){
        log.info("Creating card {}", createCardRequest);
        Card card = cardService.saveCard(createCardRequest);

        log.info("card created {}", card);

        return ResponseEntity.ok(mapToDto(card));
    }
    @PostMapping("/activate/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<String> activateCard(@PathVariable("id") Long id){
        log.info("Activating card {}", id);
        cardService.activateCard(id);
        log.info("Card was activated {}",id);
        return ResponseEntity.ok().body("Card with id " + id + " blocked");
    }

    @PostMapping("/block/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<String> blockCard(@PathVariable("id") Long id){
        log.info("Blocking card {}", id);
        cardService.blockCard(id);
        log.info("Card was blocked {}",id);
        return ResponseEntity.ok().body("Card with id " + id + " blocked");
    }

    @PostMapping("/delete/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<String> deleteCard(@PathVariable("id") Long id){
        log.info("Deleting card {}", id);
        cardService.deleteCard(id);
        log.info("Card deleted {}", id);
        return ResponseEntity.ok().body("Card with id " + id + " deleted");
    }
    @GetMapping("/getMyCards")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<List<Card>> getAllUserCards(@AuthenticationPrincipal MyUserDetails userDetails){
        Long userId = userDetails.getId();
        return ResponseEntity.ok(cardService.findByOwnerId(userId));
    }
    @GetMapping("/filter")
    public ResponseEntity<PageResponse<CardDto>> filterPosts(CardFilter filter) {
        return constructFromPage(cardService.filter(
                filter,
                PageRequest.of(filter.getPageNumber(), filter.getPageSize()))
        );
    }

    @PostMapping("/askForBlockCard/{id}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<Void> askForBlockCard(@AuthenticationPrincipal MyUserDetails userDetails,@PathVariable("id") Long id){
        log.info("Asking for block card {}", id);
        cardService.blockCardByOwnerRequest(userDetails.getId(),id);
        return ResponseEntity.noContent().build();
    }
    @PostMapping("/sendMoney")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<String> sendMoney(@AuthenticationPrincipal MyUserDetails userDetails, @RequestBody SendMoneyRequest sendMoneyRequest){
        log.info("Sending money {}", sendMoneyRequest);
        cardService.sendMoney(userDetails,sendMoneyRequest.getCardSenderId(), sendMoneyRequest.getCardReceiverId(), sendMoneyRequest.getMoneyToSend());
        return ResponseEntity.ok("Money sent");
    }

    @GetMapping("/balance/{id}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<Long> checkBalance(@AuthenticationPrincipal MyUserDetails userDetails,@PathVariable("id") Long cardId){
        log.info("Getting balance {}", cardId);
        return ResponseEntity.ok(cardService.checkBalance(userDetails,cardId));
    }
    private ResponseEntity<PageResponse<CardDto>> constructFromPage(Page<Card> page) {
        var content = page.getContent().stream()
                .map(it -> new CardDto(
                        it.getId(),
                        it.getCardNumberLast(),
                        it.getOwner(),
                        it.getExpiringAt(),
                        it.getCardStatus(),
                        it.getBalance())
                )
                .toList();
        return ResponseEntity.ok(new PageResponse<>(content, page.getTotalPages()));
    }
    private CardDto mapToDto(Card card){
        return modelMapper.map(card,CardDto.class);
    }
}
