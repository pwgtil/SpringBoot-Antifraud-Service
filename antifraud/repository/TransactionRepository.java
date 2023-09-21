package antifraud.repository;

import antifraud.entity.Transaction;
import antifraud.entity.enums.TransactionRegion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query("select distinct region from Transaction where number = :cardNumber and region != :region and date between :dateTimeFrom and :dateTimeTo")
    List<TransactionRegion> getRegionsByCardAndFromDateTime(@Param("cardNumber") String cardNumber,
                                                            @Param("region") TransactionRegion currentRegion,
                                                            @Param("dateTimeFrom") LocalDateTime dateTimeFrom,
                                                            @Param("dateTimeTo") LocalDateTime dateTimeTo);

    @Query("select distinct ip from Transaction where number = :cardNumber and ip != :ip and date between :dateTimeFrom and :dateTimeTo")
    List<String> getRegionsByIPAndFromDateTime(@Param("cardNumber") String cardNumber,
                                               @Param("ip") String currentIP,
                                               @Param("dateTimeFrom") LocalDateTime dateTimeFrom,
                                               @Param("dateTimeTo") LocalDateTime dateTimeTo);

    List<Transaction> findTransactionsByNumber(String cardNumber);

}
