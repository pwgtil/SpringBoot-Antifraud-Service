package antifraud.service;

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
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

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
        if (!complianceService.isCorrectIpAddress(transaction.getIp())
                || complianceService.isSuspiciousIP(transaction.getIp())) {
            statusLog.get(TransactionStatus.PROHIBITED).add("ip");
        }

        /*
         * 2nd level validation - stolen credit cards
         * */
        if (!complianceService.isCorrectCreditCardNumber(transaction.getNumber())
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
        log.info(regionsList.toString());
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
        log.info(ipAddressesList.toString());
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
