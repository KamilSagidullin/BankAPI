package app.api.bankapi.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.Date;

@Entity
@Table(name = "cards")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "card_number_last")
    private String cardNumberLast;

    @Column(name = "card_number_crypt")
    private String cardNumberCrypt;

    @Column(name = "owner")
    private String owner;

    @Column(name = "expires_at")
    private LocalDate expiringAt;

    @Column(name = "card_status")
    private CardStatus cardStatus;

    @Column(name = "balance")
    private Long balance;
}
