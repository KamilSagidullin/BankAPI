package app.api.bankapi.event.listener;

import app.api.bankapi.event.AskForBlockCardEvent;
import app.api.bankapi.service.CardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AskForBlockCardListener {
    private final CardService cardService;

    @EventListener
    public void onEvent(AskForBlockCardEvent event){
        log.info("Blocking card {}", event.getCardId());
        cardService.blockCard(event.getCardId());
    }
}
