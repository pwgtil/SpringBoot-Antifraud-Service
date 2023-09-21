package antifraud.entity;

import antifraud.entity.enums.TransactionRegion;
import antifraud.entity.enums.TransactionStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "amount")
    private Long amount;

    @Column(name = "ip")
    private String ip;

    @Column(name = "number")
    private String number;

    @Column(name = "region")
    @Enumerated(EnumType.STRING)
    private TransactionRegion region;

    @Column(name = "date")
    @Temporal(value = TemporalType.TIMESTAMP)
    private LocalDateTime date;

    @Column(name = "result")
    @Enumerated(EnumType.STRING)
    private TransactionStatus result;

    @Column(name = "info")
    private String info;

    @Column(name = "feedback")
    private TransactionStatus feedback = TransactionStatus.INITIAL;

    @Builder
    private Transaction(Long amount, String ip, String number, TransactionRegion region, LocalDateTime date, TransactionStatus result, String info, TransactionStatus feedback) {
        this.amount = amount;
        this.ip = ip;
        this.number = number;
        this.region = region;
        this.date = date;
        this.result = result;
        this.info = info;
        this.feedback = feedback;
    }

    public void setFeedback(TransactionStatus feedback) {
        this.feedback = feedback;
    }
}
