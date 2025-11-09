package app.api.bankapi.controller;

import app.api.bankapi.dto.CardDto;
import app.api.bankapi.entity.Card;
import app.api.bankapi.entity.CardStatus;
import app.api.bankapi.service.CardService;
import app.api.bankapi.util.CreateCardRequest;
import app.api.bankapi.util.MaskingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
        Card card = mapFromCreateRequestToCard(createCardRequest);
        cardService.saveCard(card);

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
    private Card mapFromCreateRequestToCard(CreateCardRequest createCardRequest){
        Card card = modelMapper.map(createCardRequest,Card.class);
        card.setCardNumberCrypt(maskingService.hashCard(createCardRequest.getCardNumber()));
        card.setCardNumberLast(maskingService.anonimizeCard(createCardRequest.getCardNumber()));
        card.setBalance(0L);
        card.setCardStatus(CardStatus.CREATED);

        return card;
    }
    private CardDto mapToDto(Card card){
        return modelMapper.map(card,CardDto.class);
    }
}
