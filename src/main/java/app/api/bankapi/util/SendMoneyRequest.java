package app.api.bankapi.util;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendMoneyRequest {
    @NotNull(message = "Поле не должно быть пустым")
    private Long cardSenderId;

    @NotNull(message = "Поле не должно быть пустым")
    private Long cardReceiverId;

    @NotNull
    @Positive
    private Long moneyToSend;
}
