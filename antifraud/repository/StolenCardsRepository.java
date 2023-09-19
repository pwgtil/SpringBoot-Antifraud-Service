package antifraud.repository;

import antifraud.entity.StolenCard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StolenCardsRepository extends JpaRepository<StolenCard, Long> {

    Optional<StolenCard> findStolenCardByNumber(String number);

    void deleteByNumber(String number);
}
