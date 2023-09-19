package antifraud.repository;

import antifraud.entity.SuspiciousIP;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SuspiciousIPsRepository extends JpaRepository<SuspiciousIP, Long> {

    Optional<SuspiciousIP> findSuspiciousIPByIp(String ip);

    void deleteByIp(String ip);
}
