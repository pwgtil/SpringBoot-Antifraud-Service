package antifraud.service;

import antifraud.entity.StolenCard;
import antifraud.entity.SuspiciousIP;
import antifraud.entity.enums.TransactionStatus;
import antifraud.repository.StolenCardsRepository;
import antifraud.repository.SuspiciousIPsRepository;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Getter
public class ComplianceService {

    private final Long allowedThreshold;
    private final Long manualThreshold;

    private final SuspiciousIPsRepository suspiciousIPsRepository;
    private final StolenCardsRepository stolenCardsRepository;

    @Autowired
    public ComplianceService(SuspiciousIPsRepository suspiciousIPsRepository,
                             StolenCardsRepository stolenCardsRepository,
                             @Value("${antifraudsystem.allowed-limit}") Long allowedThreshold,
                             @Value("${antifraudsystem.manual-limit}") Long manualThreshold) {
        this.allowedThreshold = allowedThreshold;
        this.manualThreshold = manualThreshold;
        this.suspiciousIPsRepository = suspiciousIPsRepository;
        this.stolenCardsRepository = stolenCardsRepository;
    }

    public TransactionStatus verifyAmount(Long amount) {
        if (amount < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount cannot be negative or equal zero!");
        } else if (amount <= allowedThreshold) {
            return TransactionStatus.ALLOWED;
        } else if (amount <= manualThreshold) {
            return TransactionStatus.MANUAL_PROCESSING;
        } else {
            return TransactionStatus.PROHIBITED;
        }
    }

    public boolean isCorrectIpAddress(String address) {
        String regexPatter = "\\b(1?\\d{1,2}|2[0-4]\\d|25[0-5])" +
                "\\.(1?\\d{1,2}|2[0-4]\\d|25[0-5])" +
                "\\.(1?\\d{1,2}|2[0-4]\\d|25[0-5])" +
                "\\.(1?\\d{1,2}|2[0-4]\\d|25[0-5])\\b";
        return address.matches(regexPatter);
    }

    public boolean isCorrectCreditCardNumber(String cardNo) {
        int nDigits = cardNo.length();

        int nSum = 0;
        boolean isSecond = false;
        for (int i = nDigits - 1; i >= 0; i--)
        {

            int d = cardNo.charAt(i) - '0';

            if (isSecond == true)
                d = d * 2;

            // We add two digits to handle
            // cases that make two digits
            // after doubling
            nSum += d / 10;
            nSum += d % 10;

            isSecond = !isSecond;
        }
        return (nSum % 10 == 0);
    }

    public boolean isStolenCard(String number) {
        return stolenCardsRepository.findStolenCardByNumber(number).isPresent();
    }

    public boolean isSuspiciousIP(String ip) {
        return suspiciousIPsRepository.findSuspiciousIPByIp(ip).isPresent();
    }

    public SuspiciousIP registerSuspiciousIP(SuspiciousIP suspiciousIP) {
        if (suspiciousIPsRepository.findSuspiciousIPByIp(suspiciousIP.getIp()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "IP address already registered");
        }

        if (!isCorrectIpAddress(suspiciousIP.getIp())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "IP address format is wrong");
        }

        suspiciousIP = suspiciousIPsRepository.save(suspiciousIP);
        return suspiciousIP;
    }

    public List<SuspiciousIP> getAllSuspiciousIPs() {
        return suspiciousIPsRepository.findAll();
    }

    public void deleteSuspiciousIP(String ip) {
        if (suspiciousIPsRepository.findSuspiciousIPByIp(ip).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "IP address not found");
        }
        suspiciousIPsRepository.deleteByIp(ip);
    }

    public StolenCard registerStolenCard(StolenCard stolenCard) {
        if (stolenCardsRepository.findStolenCardByNumber(stolenCard.getNumber()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Card number already registered");
        }

        if (!isCorrectCreditCardNumber(stolenCard.getNumber())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Card number format is wrong");
        }

        stolenCard = stolenCardsRepository.save(stolenCard);
        return stolenCard;
    }

    public List<StolenCard> getAllStolenCards() {
        return stolenCardsRepository.findAll();
    }

    public void deleteStolenCard(String number) {
        if (stolenCardsRepository.findStolenCardByNumber(number).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Card number not found");
        }
        stolenCardsRepository.deleteByNumber(number);
    }
}
