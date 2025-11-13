package app.api.bankapi.service;

import app.api.bankapi.entity.Card;
import app.api.bankapi.entity.CardStatus;
import app.api.bankapi.event.AskForBlockCardEvent;
import app.api.bankapi.exception.CardNotFoundException;
import app.api.bankapi.exception.InvalidCardStatusException;
import app.api.bankapi.exception.LackOfMoneyException;
import app.api.bankapi.repository.CardRepository;
import app.api.bankapi.repository.specification.CardFilter;
import app.api.bankapi.repository.specification.CardSpecification;
import app.api.bankapi.security.MyUserDetails;
import app.api.bankapi.util.CreateCardRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CardService {
    private final CardRepository cardRepository;
    private final MaskingService maskingService;
    private final ApplicationEventPublisher publisher;
    private final ModelMapper modelMapper;
    @Transactional(propagation = Propagation.REQUIRED,isolation = Isolation.READ_COMMITTED)
    public Card saveCard(CreateCardRequest createCardRequest){
        Card card = modelMapper.map(createCardRequest,Card.class);
        card.setCardNumberCrypt(maskingService.hashCard(createCardRequest.getCardNumber()));
        card.setCardNumberLast(maskingService.anonimizeCard(createCardRequest.getCardNumber()));
        card.setBalance(0L);
        card.setCardStatus(CardStatus.CREATED);

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
    public List<Card> findByOwnerId(Long userId){
        List<Card> cards =  cardRepository.findByOwnerId(userId);
        if (cards == null) throw new CardNotFoundException("У вас нет карт");
        return cards;
    }

    public Page<Card> filter(CardFilter filter, Pageable pageable) {
        log.info("Filter cards by data: {}", filter);
        return cardRepository.findAll(CardSpecification.withFilter(filter), pageable);
    }

    public Long checkBalance(MyUserDetails userDetails, Long cardId){
        if (!cardRepository.existsByIdAndOwner_Id(cardId, userDetails.getId())){
            throw new CardNotFoundException("Card was not found");
        }
        Card card = cardRepository.findById(cardId).get();
        return card.getBalance();
    }

    @Transactional(propagation = Propagation.REQUIRED,isolation = Isolation.REPEATABLE_READ)
    public void sendMoney(MyUserDetails owner,Long whoSend, Long whoReceive, Long amount){
        if (!(cardRepository.existsByIdAndOwner_Id(whoSend, owner.getId()) && cardRepository.existsByIdAndOwner_Id(whoReceive, owner.getId()))){
            throw new CardNotFoundException("Card was not found");
        }
        if (cardRepository.findById(whoSend).get().getBalance() - amount < 0){
            throw new LackOfMoneyException("Not enough money to send");
        }
        Card cardWhoSend = cardRepository.findById(whoSend).get();
        Card cardWhoReceive = cardRepository.findById(whoReceive).get();

        if ((cardWhoSend.getCardStatus() == CardStatus.ACTIVATED) && (cardWhoReceive.getCardStatus() == CardStatus.ACTIVATED)) {
            cardWhoSend.setBalance(cardWhoSend.getBalance() - amount);
            cardWhoReceive.setBalance(cardWhoReceive.getBalance() + amount);
            cardRepository.save(cardWhoSend);
            cardRepository.save(cardWhoReceive);
        }
        else throw new InvalidCardStatusException("Card-sender or Card-receiver is not activated");
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
    @Transactional
    public void blockCardByOwnerRequest(Long ownerId, Long cardId){
        if (cardRepository.existsByIdAndOwner_Id(cardId,ownerId)) {
            log.info("Blocking card by owner request");
            publisher.publishEvent(new AskForBlockCardEvent(this, ownerId, cardId));
        }
        else throw new CardNotFoundException("Card was not found or is not yours");

        }

    }
