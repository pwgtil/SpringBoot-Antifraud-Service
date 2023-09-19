package antifraud.service;

import antifraud.dto.TransactionDTO;
import antifraud.entity.StolenCard;
import antifraud.entity.SuspiciousIP;
import antifraud.entity.Transaction;
import antifraud.entity.enums.TransactionStatus;
import antifraud.repository.TransactionRepository;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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
        List<String> info = new ArrayList<>();
        TransactionStatus status;

        /*
         * 1st level validation - Amount
         * */
        status = complianceService.verifyAmount(transaction.getAmount());
        if (status != TransactionStatus.ALLOWED) {
            info.add("amount");
        }

        /*
         * 2nd level validation - Suspicious IP
         * */
        if (!complianceService.isCorrectIpAddress(transaction.getIp())
                || complianceService.isSuspiciousIP(transaction.getIp())) {
            status = TransactionStatus.PROHIBITED;
            info.add("ip");
        }

        /*
         * 3rd level validation - stolen credit cards
         * */
        if (!complianceService.isCorrectCreditCardNumber(transaction.getNumber())
                || complianceService.isStolenCard(transaction.getNumber())) {
            status = TransactionStatus.PROHIBITED;
            info.add("card-number");
        }

        /*
         * Finalization
         * */
        if (info.isEmpty()) {
            info.add("none");
        }
        info.sort(Comparator.naturalOrder());

//        String infoString = String.join(", ", info);
        String infoString = info.stream().reduce((s1, s2) -> s1 + ", " + s2).orElse("");

        transaction.setResult(status);
        transaction.setInfo(infoString);
        saveTransaction(transaction);
    }

    private void saveTransaction(TransactionDTO input) {
        Transaction transaction = Transaction.builder()
                .amount(input.getAmount())
                .ip(input.getIp())
                .number(input.getNumber())
                .date(input.getDate())
                .region(input.getRegion())
                .result(input.getResult())
                .info(input.getInfo())
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
}
