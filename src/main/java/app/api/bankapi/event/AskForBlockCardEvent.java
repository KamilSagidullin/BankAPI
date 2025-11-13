package app.api.bankapi.event;

import lombok.*;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.EventListener;

@Getter
public class AskForBlockCardEvent extends ApplicationEvent {
    private Long ownerId;
    private Long cardId;

    public AskForBlockCardEvent(Object source,Long ownerId,Long cardId) {
        super(source);
        this.ownerId = ownerId;
        this.cardId = cardId;
    }
}
