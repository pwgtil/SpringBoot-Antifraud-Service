package antifraud.service;

import antifraud.entity.StolenCard;
import antifraud.entity.SuspiciousIP;
import antifraud.entity.enums.TransactionStatus;
import antifraud.repository.StolenCardsRepository;
import antifraud.repository.SuspiciousIPsRepository;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.function.Consumer;

@Service
@Getter
@Slf4j
public class ComplianceService {

//    private final Long allowedThreshold;
//    private final Long manualThreshold;

    private final SuspiciousIPsRepository suspiciousIPsRepository;
    private final StolenCardsRepository stolenCardsRepository;
    private ConfigService limits;
    private final Consumer<Long>[][] limitUpdateMatrix = new Consumer[3][3];

    @Autowired
    public ComplianceService(SuspiciousIPsRepository suspiciousIPsRepository,
                             StolenCardsRepository stolenCardsRepository,
                             ConfigService limits)
//                             @Value("${antifraudsystem.allowed-limit}") Long allowedThreshold,
//                             @Value("${antifraudsystem.manual-limit}") Long manualThreshold)
    {
//        this.allowedThreshold = allowedThreshold;
//        this.manualThreshold = manualThreshold;
        this.suspiciousIPsRepository = suspiciousIPsRepository;
        this.stolenCardsRepository = stolenCardsRepository;
        this.limits = limits;
        fillTransactionLimitMatrix();
    }

    private void fillTransactionLimitMatrix() {
        limitUpdateMatrix[0][0] = limitUpdateException;
        limitUpdateMatrix[1][1] = limitUpdateException;
        limitUpdateMatrix[2][2] = limitUpdateException;
        limitUpdateMatrix[1][0] = increaseMaxAllowed;
        limitUpdateMatrix[2][0] = increaseMaxBoth;
        limitUpdateMatrix[0][1] = decreaseMaxAllowed;
        limitUpdateMatrix[2][1] = increaseMaxManual;
        limitUpdateMatrix[0][2] = decreaseMaxBoth;
        limitUpdateMatrix[1][2] = decreaseMaxManual;
    }

    public TransactionStatus verifyAmount(Long amount) {
        if (amount < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount cannot be negative or equal zero!");
        } else if (amount <= limits.getMaxAllowed()) {
            return TransactionStatus.ALLOWED;
        } else if (amount <= limits.getMaxManual()) {
            return TransactionStatus.MANUAL_PROCESSING;
        } else {
            return TransactionStatus.PROHIBITED;
        }
    }

    public boolean isNotCorrectIpAddress(String address) {
        String regexPatter = "\\b(1?\\d{1,2}|2[0-4]\\d|25[0-5])" +
                "\\.(1?\\d{1,2}|2[0-4]\\d|25[0-5])" +
                "\\.(1?\\d{1,2}|2[0-4]\\d|25[0-5])" +
                "\\.(1?\\d{1,2}|2[0-4]\\d|25[0-5])\\b";
        return !address.matches(regexPatter);
    }

    public boolean isNotCorrectCreditCardNumber(String cardNo) {
        int nDigits = cardNo.length();

        int nSum = 0;
        boolean isSecond = false;
        for (int i = nDigits - 1; i >= 0; i--) {

            int d = cardNo.charAt(i) - '0';

            if (isSecond)
                d = d * 2;

            // We add two digits to handle
            // cases that make two digits
            // after doubling
            nSum += d / 10;
            nSum += d % 10;

            isSecond = !isSecond;
        }
        return (nSum % 10 != 0);
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

        if (isNotCorrectIpAddress(suspiciousIP.getIp())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "IP address format is wrong");
        }

        suspiciousIP = suspiciousIPsRepository.save(suspiciousIP);
        return suspiciousIP;
    }

    public List<SuspiciousIP> getAllSuspiciousIPs() {
        return suspiciousIPsRepository.findAll();
    }

    @Transactional
    public void deleteSuspiciousIP(String ip) {
        if (isNotCorrectIpAddress(ip)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "IP address format is wrong");
        }

        if (suspiciousIPsRepository.findSuspiciousIPByIp(ip).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "IP address not found");
        }

        suspiciousIPsRepository.deleteByIp(ip);
    }

    public StolenCard registerStolenCard(StolenCard stolenCard) {
        if (stolenCardsRepository.findStolenCardByNumber(stolenCard.getNumber()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Card number already registered");
        }

        if (isNotCorrectCreditCardNumber(stolenCard.getNumber())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Card number format is wrong");
        }

        stolenCard = stolenCardsRepository.save(stolenCard);
        return stolenCard;
    }

    public List<StolenCard> getAllStolenCards() {
        return stolenCardsRepository.findAll();
    }

    @Transactional
    public void deleteStolenCard(String number) {
        if (isNotCorrectCreditCardNumber(number)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Card number format is wrong");
        }

        if (stolenCardsRepository.findStolenCardByNumber(number).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Card number not found");
        }

        stolenCardsRepository.deleteByNumber(number);
    }


    /*
     * Functional programming for fun
     * */
    public void updateTransactionLimit(TransactionStatus result, TransactionStatus feedback, long amount) {
        log.info("Amounts before update [allowed / manual]: " + limits.getMaxAllowed() + " / " + limits.getMaxManual());
        limitUpdateMatrix[result.ordinal()][feedback.ordinal()].accept(amount);
        log.info("Amounts after update [allowed / manual]: " + limits.getMaxAllowed() + " / " + limits.getMaxManual());
    }

    // new_limit = 0.8 * current_limit + 0.2 * value_from_transaction
    Consumer<Long> increaseMaxAllowed = (amount) -> limits.setMaxAllowed(Double.valueOf(Math.ceil(0.8 * limits.getMaxAllowed() + 0.2 * amount)).longValue());

    Consumer<Long> increaseMaxManual = (amount) -> limits.setMaxManual(Double.valueOf(Math.ceil(0.8 * limits.getMaxManual() + 0.2 * amount)).longValue());

    Consumer<Long> increaseMaxBoth = (amount) -> {
        increaseMaxAllowed.accept(amount);
        increaseMaxManual.accept(amount);
    };

    // new_limit = 0.8 * current_limit - 0.2 * value_from_transaction
    Consumer<Long> decreaseMaxAllowed = (amount) -> limits.setMaxAllowed(Double.valueOf(Math.ceil(0.8 * limits.getMaxAllowed() - 0.2 * amount)).longValue());

    Consumer<Long> decreaseMaxManual = (amount) -> limits.setMaxManual(Double.valueOf(Math.ceil(0.8 * limits.getMaxManual() - 0.2 * amount)).longValue());

    Consumer<Long> decreaseMaxBoth = (amount) -> {
        decreaseMaxAllowed.accept(amount);
        decreaseMaxManual.accept(amount);
    };

    Consumer<Long> limitUpdateException = (amount) -> {
        throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Feedback should be different than result!");
    };
}


