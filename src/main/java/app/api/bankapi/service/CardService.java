package app.api.bankapi.service;

import app.api.bankapi.entity.Card;
import app.api.bankapi.entity.CardStatus;
import app.api.bankapi.exception.CardNotFoundException;
import app.api.bankapi.exception.LackOfMoneyException;
import app.api.bankapi.repository.CardRepository;
import app.api.bankapi.util.MaskingService;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;

@Service
@RequiredArgsConstructor
public class CardService {
    private final CardRepository cardRepository;
    private final MaskingService maskingService;
    @Transactional(propagation = Propagation.REQUIRED,isolation = Isolation.READ_COMMITTED)
    public Card saveCard(Card card){
         return cardRepository.save(card);

    }
    public Card findById(Long id){
        return cardRepository.findById(id).orElseThrow(() ->new CardNotFoundException("Card was not found"));
    }
    public List<Card> findAll(){
        return cardRepository.findAll();
    }
    public Page<Card> findAll(Pageable pageable){
        return cardRepository.findAll(pageable);
    }
    @Transactional(propagation = Propagation.REQUIRED,isolation = Isolation.REPEATABLE_READ)
    public void sendMoney(String whoSend,String whoReceive, Long amount){
        if (cardRepository.findByCardNumberCrypt(maskingService.hashCard(whoSend)).isEmpty() || cardRepository.findByCardNumberCrypt(maskingService.hashCard(whoReceive)).isEmpty()){
            throw new CardNotFoundException("Card was not found");
        }
        if (cardRepository.findByCardNumberCrypt(maskingService.hashCard(whoSend)).get().getBalance() - amount < 0){
            throw new LackOfMoneyException("Not enough money to send");
        }
        Card cardWhoSend = cardRepository.findByCardNumberCrypt(maskingService.hashCard(whoSend)).get();
        Card cardWhoReceive = cardRepository.findByCardNumberCrypt(maskingService.hashCard(whoReceive)).get();
        cardWhoSend.setBalance(cardWhoSend.getBalance() - amount);
        cardWhoReceive.setBalance(cardWhoReceive.getBalance() + amount);
    }
    @Transactional
    public void activateCard(Long id){
        Card card = cardRepository.findById(id).orElseThrow(() ->  new CardNotFoundException("Card was not found"));
        card.setCardStatus(CardStatus.ACTIVATED);
        cardRepository.save(card);
    }
    @Transactional
    public void blockCard(Long id){
        Card card = cardRepository.findById(id).orElseThrow(() ->  new CardNotFoundException("Card was not found"));
        card.setCardStatus(CardStatus.BLOCKED);
        cardRepository.save(card);
    }
    @Transactional
    public void deleteCard(Long id){
        Card card = cardRepository.findById(id).orElseThrow(() ->  new CardNotFoundException("Card was not found"));
        cardRepository.delete(card);
    }


}
