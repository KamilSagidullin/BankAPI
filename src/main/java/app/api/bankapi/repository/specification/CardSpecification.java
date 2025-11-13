package app.api.bankapi.repository.specification;

import app.api.bankapi.entity.Card;
import app.api.bankapi.entity.CardStatus;
import org.springframework.data.jpa.domain.Specification;
import java.time.LocalDate;

public class CardSpecification {

    public static Specification<Card> withFilter(CardFilter filter) {
        return Specification.allOf(
                balanceEquals(filter.getBalance()),
                statusEquals(filter.getCardStatus()),
                expiringAtEquals(filter.getExpiringAt())
        );
    }

    private static Specification<Card> balanceEquals(Long balance) {
        return (root, query, cb) ->
                balance == null ? null : cb.equal(root.get("balance"), balance);
    }

    private static Specification<Card> statusEquals(CardStatus status) {
        return (root, query, cb) ->
                status == null ? null : cb.equal(root.get("cardStatus"), status);
    }

    private static Specification<Card> expiringAtEquals(LocalDate date) {
        return (root, query, cb) ->
                date == null ? null : cb.equal(root.get("expiringAt"), date);
    }
}
