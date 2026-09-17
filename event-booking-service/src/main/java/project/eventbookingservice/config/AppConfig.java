package project.eventbookingservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.web.reactive.function.client.WebClient;
import project.commonutils.config.CommonConfig;

@Configuration
public class AppConfig extends CommonConfig {

    @Bean
    @LoadBalanced
    public WebClient.Builder webClientBuilder() {
        return super.webClientBuilder();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return super.objectMapper();
    }

    @Bean
    public AuditorAware<String> auditorAware() {
        return super.auditorAware();
    }
}