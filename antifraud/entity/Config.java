package antifraud.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "config")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Config {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String name;

    @Column(name = "limit_value")
    private Long value;

    private String text;

    public Config(String name, Long value, String text) {
        this.name = name;
        this.value = value;
        this.text = text;
    }
}
