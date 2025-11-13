package app.api.bankapi.repository.specification;

import app.api.bankapi.entity.CardStatus;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CardFilter {
    private Long balance;

    private CardStatus cardStatus;

    private LocalDate expiringAt;

    private Integer pageSize = 10;

    private Integer pageNumber = 0;

}
