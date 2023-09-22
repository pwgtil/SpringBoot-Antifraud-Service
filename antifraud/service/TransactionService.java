package antifraud.service;

import antifraud.dto.FeedbackDTO;
import antifraud.dto.TransactionDTO;
import antifraud.entity.StolenCard;
import antifraud.entity.SuspiciousIP;
import antifraud.entity.Transaction;
import antifraud.entity.enums.TransactionRegion;
import antifraud.entity.enums.TransactionStatus;
import antifraud.repository.TransactionRepository;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@NoArgsConstructor
public class TransactionService {

    private ComplianceService complianceService;
    private TransactionRepository transactionRepository;

    @Autowired
    public TransactionService(ComplianceService complianceService, TransactionRepository transactionRepository) {
        this.complianceService = complianceService;
        this.transactionRepository = transactionRepository;
    }


    public void performTransaction(TransactionDTO transaction) {

        Map<TransactionStatus, List<String>> statusLog = Map.of(
                TransactionStatus.PROHIBITED, new ArrayList<>(),
                TransactionStatus.MANUAL_PROCESSING, new ArrayList<>(),
                TransactionStatus.ALLOWED, new ArrayList<>(List.of("none"))
        );

        /*
         * 1st level validation - Suspicious IP
         * */
        if (complianceService.isNotCorrectIpAddress(transaction.getIp())
                || complianceService.isSuspiciousIP(transaction.getIp())) {
            statusLog.get(TransactionStatus.PROHIBITED).add("ip");
        }

        /*
         * 2nd level validation - stolen credit cards
         * */
        if (complianceService.isNotCorrectCreditCardNumber(transaction.getNumber())
                || complianceService.isStolenCard(transaction.getNumber())) {
            statusLog.get(TransactionStatus.PROHIBITED).add("card-number");
        }

        /*
         * 3rd level validation - Amount
         * */
        TransactionStatus amountStatus = complianceService.verifyAmount(transaction.getAmount());
        if (amountStatus != TransactionStatus.ALLOWED) {
            statusLog.get(amountStatus).add("amount");
        }

        /*
         * 4th level validation - card-correlation
         * */
        List<TransactionRegion> regionsList = transactionRepository
                .getRegionsByCardAndFromDateTime(
                        transaction.getNumber(),
                        transaction.getRegion(),
                        transaction.getDate().minusMinutes(60L),
                        transaction.getDate()
                );
        if (regionsList.size() >= 2) {
            TransactionStatus cardCorrelationStatus;
            if (regionsList.size() > 2) {
                cardCorrelationStatus = TransactionStatus.PROHIBITED;
            } else {
                cardCorrelationStatus = TransactionStatus.MANUAL_PROCESSING;
            }
            statusLog.get(cardCorrelationStatus).add("region-correlation");
        }

        /*
         * 5th level validation - ip-correlation
         * */
        List<String> ipAddressesList = transactionRepository
                .getRegionsByIPAndFromDateTime(
                        transaction.getNumber(),
                        transaction.getIp(),
                        transaction.getDate().minusMinutes(60L),
                        transaction.getDate()
                );
        if (ipAddressesList.size() >= 2) {
            TransactionStatus ipCorrelationStatus;
            if (ipAddressesList.size() > 2) {
                ipCorrelationStatus = TransactionStatus.PROHIBITED;
            } else {
                ipCorrelationStatus = TransactionStatus.MANUAL_PROCESSING;
            }
            statusLog.get(ipCorrelationStatus).add("ip-correlation");
        }

        /*
         * Finalization
         * */
        TransactionStatus status;
        List<String> info;
        if (!statusLog.get(TransactionStatus.PROHIBITED).isEmpty()) {
            status = TransactionStatus.PROHIBITED;
            info = statusLog.get(TransactionStatus.PROHIBITED);
        } else if (!statusLog.get(TransactionStatus.MANUAL_PROCESSING).isEmpty()) {
            status = TransactionStatus.MANUAL_PROCESSING;
            info = statusLog.get(TransactionStatus.MANUAL_PROCESSING);
        } else {
            status = TransactionStatus.ALLOWED;
            info = statusLog.get(TransactionStatus.ALLOWED);
        }

        info.sort(Comparator.naturalOrder());

//        String infoString = String.join(", ", info);
        String infoString = info.stream().reduce((s1, s2) -> s1 + ", " + s2).orElse("");

        transaction.setResult(status);
        transaction.setInfo(infoString);
        saveNewTransaction(transaction);
    }

    private void saveNewTransaction(TransactionDTO input) {
        Transaction transaction = Transaction.builder()
                .amount(input.getAmount())
                .ip(input.getIp())
                .number(input.getNumber())
                .date(input.getDate())
                .region(input.getRegion())
                .result(input.getResult())
                .info(input.getInfo())
                .feedback(TransactionStatus.INITIAL)
                .build();
        transactionRepository.save(transaction);
    }

    public StolenCard registerStolenCard(StolenCard stolenCard) {
        return complianceService.registerStolenCard(stolenCard);
    }

    public List<StolenCard> getAllStolenCards() {
        return complianceService.getAllStolenCards();
    }

    public void deleteStolenCard(String number) {
        complianceService.deleteStolenCard(number);
    }

    public SuspiciousIP registerSuspiciousIP(SuspiciousIP suspiciousIP) {
        return complianceService.registerSuspiciousIP(suspiciousIP);
    }

    public List<SuspiciousIP> getAllSuspiciousIPs() {
        return complianceService.getAllSuspiciousIPs();
    }

    public void deleteSuspiciousIP(String ip) {
        complianceService.deleteSuspiciousIP(ip);
    }

    @Transactional
    public FeedbackDTO addFeedback(FeedbackDTO feedback) {
        Transaction transaction = transactionRepository
                .findById(feedback.getTransactionId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction not found"));
        TransactionStatus feedbackStatus;
        try {
            feedbackStatus = TransactionStatus.valueOf(feedback.getFeedback());
            if (feedbackStatus == TransactionStatus.INITIAL) {
                throw new IllegalArgumentException();
            }
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Incorrect feedback value. Choose from ALLOWED, MANUAL_PROCESSING & PROHIBITED");
        }
        if (!(transaction.getFeedback() == TransactionStatus.INITIAL || transaction.getFeedback() == null)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Feedback already provided for this transaction");
        }
        complianceService.updateTransactionLimit(transaction.getResult(), feedbackStatus, transaction.getAmount());

        transaction.setFeedback(feedbackStatus);
        
        transactionRepository.save(transaction);
        
        return new FeedbackDTO(transaction.getId(),
                transaction.getAmount(),
                transaction.getResult(),
                transaction.getIp(),
                transaction.getNumber(),
                transaction.getInfo(),
                transaction.getRegion(),
                transaction.getDate(),
                transaction.getFeedback());
    }

    public List<FeedbackDTO> getTransactionHistory(String cardNumber) {
        List<Transaction> list;

        if (cardNumber.isBlank()) {
            list = transactionRepository.findAll();
        } else {
            if (complianceService.isNotCorrectCreditCardNumber(cardNumber)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Card number incorrect!");
            }
            list = transactionRepository.findTransactionsByNumber(cardNumber);
            if (list.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No transactions found for specific card number");
            }
        }
        return list.stream().map(t ->
                        new FeedbackDTO(t.getId(),
                                t.getAmount(),
                                t.getResult(),
                                t.getIp(),
                                t.getNumber(),
                                t.getInfo(),
                                t.getRegion(),
                                t.getDate(),
                                t.getFeedback()))
                .collect(Collectors.toList());
    }
}
