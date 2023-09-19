package antifraud.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "suspicious_ips")
@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class SuspiciousIP {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    @Column(name = "ip", unique = true)
    @JsonProperty(access = JsonProperty.Access.READ_WRITE)
    private String ip;
}
