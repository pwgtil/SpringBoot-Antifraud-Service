package antifraud.dto;

import antifraud.entity.enums.TransactionRegion;
import antifraud.entity.enums.TransactionStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class FeedbackDTO {


    @NotNull
    @JsonProperty(access = JsonProperty.Access.READ_WRITE)
    private Long transactionId;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long amount;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String ip;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String number;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private TransactionRegion region;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime date;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private TransactionStatus result;

    @JsonIgnore
//    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String info;

    @NotNull
    @JsonProperty(access = JsonProperty.Access.READ_WRITE)
    private String feedback;

    @Builder
    public FeedbackDTO(Long id, Long amount, TransactionStatus result, String ip, String number, String info, TransactionRegion region, LocalDateTime date, TransactionStatus feedback) {
        this.transactionId = id;
        this.amount = amount;
        this.result = result;
        this.ip = ip;
        this.number = number;
        this.info = info;
        this.region = region;
        this.date = date;
        if (feedback == TransactionStatus.INITIAL || feedback == null) {
            this.feedback = "";
        } else {
            this.feedback = feedback.name();
        }
    }
}
