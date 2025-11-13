package app.api.bankapi.dto;

import app.api.bankapi.entity.CardStatus;
import app.api.bankapi.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CardDto {
    private Long id;
    private String cardNumberLast;
    private User owner;
    private LocalDate expiringAt;
    private CardStatus cardStatus;
    private Long balance;
}
