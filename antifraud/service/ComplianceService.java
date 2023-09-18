package antifraud.service;

import antifraud.entity.enums.TransactionStatus;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@Getter
public class ComplianceService {

    private final Long allowedThreshold;
    private final Long manualThreshold;

    public ComplianceService(@Value("${antifraudsystem.allowed-limit}") Long allowedThreshold,
                             @Value("${antifraudsystem.manual-limit}") Long manualThreshold) {
        this.allowedThreshold = allowedThreshold;
        this.manualThreshold = manualThreshold;
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
}
