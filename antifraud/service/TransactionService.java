package antifraud.service;

import antifraud.dto.TransactionDTO;
import antifraud.entity.Transaction;
import antifraud.entity.enums.TransactionStatus;
import antifraud.repository.TransactionRepository;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@NoArgsConstructor
@Getter
public class TransactionService {

    private ComplianceService complianceService;
    private TransactionRepository transactionRepository;

    @Autowired
    public TransactionService(ComplianceService complianceService, TransactionRepository transactionRepository) {
        this.complianceService = complianceService;
        this.transactionRepository = transactionRepository;
    }


    public void performTransaction(TransactionDTO transaction) {
        TransactionStatus status = complianceService.verifyAmount(transaction.getAmount());
        transaction.setResult(status);
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
}
