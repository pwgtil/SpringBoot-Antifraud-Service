package antifraud.dto;

import antifraud.entity.enums.TransactionRegion;
import antifraud.entity.enums.TransactionStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.LuhnCheck;


import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class TransactionDTO {

    @JsonIgnore
//    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @NotNull
    private Long amount;

    @NotNull
    // valid IPv4 addresses
    @Pattern(regexp = "\\b(1?\\d{1,2}|2[0-4]\\d|25[0-5])" +
            "\\.(1?\\d{1,2}|2[0-4]\\d|25[0-5])" +
            "\\.(1?\\d{1,2}|2[0-4]\\d|25[0-5])" +
            "\\.(1?\\d{1,2}|2[0-4]\\d|25[0-5])\\b", message = "Incorrect IP address!")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String ip;

    @LuhnCheck(message = "Incorrect card number")
    @NotNull
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String number;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private TransactionStatus result;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String info;

    @NotNull
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private TransactionRegion region;

    @NotNull
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private LocalDateTime date;

    @Builder
    public TransactionDTO(Long id, Long amount, TransactionStatus result, String ip, String number, String info, TransactionRegion region, LocalDateTime date) {
        this.id = id;
        this.amount = amount;
        this.result = result;
        this.ip = ip;
        this.number = number;
        this.info = info;
        this.region = region;
        this.date = date;
    }
}
