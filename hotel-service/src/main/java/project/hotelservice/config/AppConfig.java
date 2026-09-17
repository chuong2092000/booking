package project.hotelservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import project.commonutils.config.CommonConfig;

@Configuration
public class AppConfig extends CommonConfig {
    @Bean
    public ObjectMapper objectMapper() {
        return super.objectMapper();
    }

    @Bean
    public AuditorAware<String> auditorAware() {
        return super.auditorAware();
    }
}