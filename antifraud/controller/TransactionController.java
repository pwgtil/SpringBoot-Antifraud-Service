package antifraud.controller;

import antifraud.controller.routing.Transaction;
import antifraud.entity.enums.TransactionStatus;
import antifraud.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import antifraud.dto.TransactionDTO;

@RestController
@Validated
public class TransactionController {

    private TransactionService transactionService;

    public TransactionController(@Autowired TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping(Transaction.PATH)
    public ResponseEntity<TransactionDTO> performTransaction(@Valid @RequestBody TransactionDTO transaction) {
        transactionService.performTransaction(transaction);
        //test
        transaction = TransactionDTO.builder().result(transaction.getResult()).amount(transaction.getAmount()).build();
        return new ResponseEntity<>(transaction, HttpStatus.OK);
    }
}
