package com.shepherdmoney.interviewproject.controller;

import com.shepherdmoney.interviewproject.vo.request.AddCreditCardToUserPayload;
import com.shepherdmoney.interviewproject.vo.request.UpdateBalancePayload;
import com.shepherdmoney.interviewproject.vo.response.CreditCardView;
import com.shepherdmoney.interviewproject.model.*;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.shepherdmoney.interviewproject.repository.UserRepository;
import com.shepherdmoney.interviewproject.repository.CreditCardRepository;



@RestController
public class CreditCardController {

    // TODO: wire in CreditCard repository here (~1 line)
    // COMPLETE

    //Going to wire both the credit card and user repositories
    private final UserRepository userRepository;
    private final CreditCardRepository creditCardRepository;

    public CreditCardController(UserRepository userRepository, CreditCardRepository creditCardRepository) {
        this.userRepository = userRepository;
        this.creditCardRepository = creditCardRepository;
    }
    @PostMapping("/credit-card")
    public ResponseEntity<Integer> addCreditCardToUser(@RequestBody AddCreditCardToUserPayload payload) {
        // TODO: Create a credit card entity, and then associate that credit card with user with given userId
        //       Return 200 OK with the credit card id if the user exists and credit card is successfully associated with the user
        //       Return other appropriate response code for other exception cases
        //       Do not worry about validating the card number, assume card number could be any arbitrary format and length
        // COMPLETE
        try{
            Optional<User> optionalUser = userRepository.findById(payload.getUserId());
            if(optionalUser.isEmpty()){
                return ResponseEntity.notFound().build();
            }

            // if such a user exists, now create the credit card
            CreditCard creditCard = new CreditCard();
            creditCard.setIssuanceBank(payload.getCardIssuanceBank());
            creditCard.setNumber(payload.getCardNumber());

            // Now add it to the appropriate user
            User user = optionalUser.get();
            creditCard.setUser(user);

            // Now save the credit card in the database
            CreditCard savedCreditCard = creditCardRepository.save(creditCard);
            return ResponseEntity.ok(savedCreditCard.getId());
        }
        catch(Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/credit-card:all")
    public ResponseEntity<List<CreditCardView>> getAllCardOfUser(@RequestParam int userId) {
        // TODO: return a list of all credit card associated with the given userId, using CreditCardView class
        //       if the user has no credit card, return empty list, never return null
        // COMPLETE
        Optional<User> optionalUser = userRepository.findById(userId);
        if(optionalUser.isEmpty()){
            return ResponseEntity.notFound().build();
        }
        List<CreditCard> creditCards = optionalUser.get().getCreditCards();

        List<CreditCardView> creditCardViews = creditCards.stream()
                .map(this::convertToCreditCardView)
                .collect(Collectors.toList());

        return ResponseEntity.ok(creditCardViews);
    }

    // Helper function to convert credit card to creditCardView
    private CreditCardView convertToCreditCardView(CreditCard creditCard) {
        CreditCardView creditCardView = new CreditCardView(creditCard.getNumber(), creditCard.getIssuanceBank());
        return creditCardView;
    }


    @GetMapping("/credit-card:user-id")
    public ResponseEntity<Integer> getUserIdForCreditCard(@RequestParam String creditCardNumber) {
        // TODO: Given a credit card number, efficiently find whether there is a user associated with the credit card
        //       If so, return the user id in a 200 OK response. If no such user exists, return 400 Bad Request
        // Find the credit card with the given creditCardNumber in the database.
        // COMPLETE
        Optional<CreditCard> optionalCreditCard = creditCardRepository.findByNumber(creditCardNumber);
        if (optionalCreditCard.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        // Get the user associated with the credit card.
        User user = optionalCreditCard.get().getUser();

        // Return the user id in a 200 OK response.
        return ResponseEntity.ok(user.getId());
    }

    @PostMapping("/credit-card:update-balance")
    public ResponseEntity<String> postMethodName(@RequestBody UpdateBalancePayload[] payload) {
        //TODO: Given a list of transactions, update credit cards' balance history.
        //      For example: if today is 4/12, a credit card's balanceHistory is [{date: 4/12, balance: 110}, {date: 4/10, balance: 100}],
        //      Given a transaction of {date: 4/10, amount: 10}, the new balanceHistory is
        //      [{date: 4/12, balance: 120}, {date: 4/11, balance: 110}, {date: 4/10, balance: 110}]
        //      Return 200 OK if update is done and successful, 400 Bad Request if the given card number
        //        is not associated with a card.
        // COMPLETE
        
        try {
            for (UpdateBalancePayload transaction : payload) {
                Optional<CreditCard> optionalCreditCard = creditCardRepository.findByNumber(transaction.getCreditCardNumber());
                if (optionalCreditCard.isEmpty()) {
                    return ResponseEntity.badRequest().body("Credit card with number " + transaction.getCreditCardNumber() + " not found.");
                }

                CreditCard creditCard = optionalCreditCard.get();

                // Create a new BalanceHistory entry for the transaction date and amount.
                BalanceHistory balanceHistory = new BalanceHistory();
                balanceHistory.setDate(transaction.getTransactionTime());
                balanceHistory.setBalance(transaction.getTransactionAmount());

                // Add the new balance history entry to the credit card's list of balance history.
                List<BalanceHistory> balanceHistoryList = creditCard.getBalanceHistory();
                balanceHistoryList.add(balanceHistory);

                // Save the updated credit card entity in the database.
                creditCardRepository.save(creditCard);
            }

            // Return 200 OK if the update is done and successful.
            return ResponseEntity.ok("Balance history updated successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
}
