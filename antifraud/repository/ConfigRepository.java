package antifraud.repository;

import antifraud.entity.Config;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface ConfigRepository extends CrudRepository<Config, Long> {
    Optional<Config> findConfigByName(String name);
}
