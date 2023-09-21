package antifraud.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "antifraudsystem")
@Getter
@Setter
public class TransactionLimitsConfig {
    private Long maxAllowed;
    private Long maxManual;
}
