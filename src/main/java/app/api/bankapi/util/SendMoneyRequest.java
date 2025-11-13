package app.api.bankapi.util;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@NoArgsConstructor
public class SendMoneyRequest {
    private Long cardSenderId;
    private Long cardReceiverId;
    private Long moneyToSend;
}
