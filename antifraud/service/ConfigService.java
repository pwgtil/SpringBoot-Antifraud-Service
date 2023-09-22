package antifraud.service;

import antifraud.entity.Config;
import antifraud.repository.ConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConfigService {
    public final String PARAM_ALLOWED_LIMIT = "PARAM_ALLOWED_LIMIT";
    public final String PARAM_MANUAL_LIMIT = "PARAM_MANUAL_LIMIT";

    private final ConfigRepository configRepository;

    private volatile Config allowedLimit;
    private volatile Config manualLimit;

    @Autowired
    public ConfigService(ConfigRepository configRepository) {
        this.configRepository = configRepository;
        try {
            configRepository.save(new Config(PARAM_ALLOWED_LIMIT, 200L, "This is parameter to keep global allowed limit per transaction"));
            configRepository.save(new Config(PARAM_MANUAL_LIMIT, 1500L, "This is parameter to keep global manual limit per transaction"));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        allowedLimit = configRepository.findConfigByName(PARAM_ALLOWED_LIMIT)
                .orElseThrow(() -> new RuntimeException("Parameters cannot be setup"));
        manualLimit = configRepository.findConfigByName(PARAM_MANUAL_LIMIT)
                .orElseThrow(() -> new RuntimeException("Parameters cannot be setup"));
    }

    public long getMaxAllowed() {
        allowedLimit = configRepository.findConfigByName(PARAM_ALLOWED_LIMIT)
                .orElseThrow(() -> new RuntimeException("Parameter PARAM_ALLOWED_LIMIT cannot be found"));
        return allowedLimit.getValue();
    }

    public long getMaxManual() {
        manualLimit = configRepository.findConfigByName(PARAM_MANUAL_LIMIT)
                .orElseThrow(() -> new RuntimeException("Parameter PARAM_MANUAL_LIMIT cannot be found"));
        return manualLimit.getValue();
    }

    @Transactional
    public void setMaxAllowed(long value) {
        allowedLimit.setValue(value);
        configRepository.save(allowedLimit);
    }

    @Transactional
    public void setMaxManual(long value) {
        manualLimit.setValue(value);
        configRepository.save(manualLimit);
    }

}
