package app.api.bankapi.dto;

import app.api.bankapi.entity.CardStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
@Data
@Builder
public class CardDto {
    private Long id;
    private String cardNumberLast;
    private String owner;
    private LocalDate expiringAt;
    private CardStatus cardStatus;
    private Long balance;
}
