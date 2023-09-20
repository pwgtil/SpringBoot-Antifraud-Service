package antifraud.controller;

import antifraud.entity.StolenCard;
import antifraud.entity.SuspiciousIP;
import antifraud.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import antifraud.dto.TransactionDTO;

import java.util.List;
import java.util.Map;

@RestController
@Validated
public class TransactionController {

    private TransactionService transactionService;

    public TransactionController(@Autowired TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    /*
     * Transactions section
     * */
    @PostMapping(antifraud.controller.routing.Transaction.PATH)
    public ResponseEntity<TransactionDTO> performTransaction(@Valid @RequestBody TransactionDTO transaction) {
        transactionService.performTransaction(transaction);
        //test
        transaction = TransactionDTO.builder()
                .result(transaction.getResult())
                .info(transaction.getInfo())
                .build();
        return new ResponseEntity<>(transaction, HttpStatus.OK);
    }

    /*
     * Stolen Cards section
     * */
    @PostMapping(antifraud.controller.routing.StolenCard.PATH)
    public ResponseEntity<StolenCard> addStolenCard(@RequestBody StolenCard stolenCard) {
        StolenCard card = transactionService.registerStolenCard(stolenCard);
        return new ResponseEntity<>(card, HttpStatus.OK);
    }

    @GetMapping(antifraud.controller.routing.StolenCard.PATH)
    public ResponseEntity<List<StolenCard>> addStolenCard() {
        return new ResponseEntity<>(transactionService.getAllStolenCards(), HttpStatus.OK);
    }

    @DeleteMapping(antifraud.controller.routing.StolenCard.PATH + "/{number}")
    public ResponseEntity<Map<String, String>> addStolenCard(@PathVariable String number) {
        transactionService.deleteStolenCard(number);
        return new ResponseEntity<>(Map.of("status", "Card " + number + " successfully removed!"), HttpStatus.OK);
    }

    /*
     * Suspicious section
     * */
    @PostMapping(antifraud.controller.routing.SuspiciousIP.PATH)
    public ResponseEntity<SuspiciousIP> addSuspiciousIP(@RequestBody SuspiciousIP suspiciousIP) {
        SuspiciousIP ip = transactionService.registerSuspiciousIP(suspiciousIP);
        return new ResponseEntity<>(ip, HttpStatus.OK);
    }

    @GetMapping(antifraud.controller.routing.SuspiciousIP.PATH)
    public ResponseEntity<List<SuspiciousIP>> addSuspiciousIP() {
        return new ResponseEntity<>(transactionService.getAllSuspiciousIPs(), HttpStatus.OK);
    }

    @DeleteMapping(antifraud.controller.routing.SuspiciousIP.PATH + "/{ip}")
    public ResponseEntity<Map<String, String>> addSuspiciousIP(@PathVariable String ip) {
        transactionService.deleteSuspiciousIP(ip);
        return new ResponseEntity<>(Map.of("status", "IP " + ip + " successfully removed!"), HttpStatus.OK);
    }


}
