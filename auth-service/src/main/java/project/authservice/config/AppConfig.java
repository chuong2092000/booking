package project.authservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import project.commonutils.config.CommonConfig;

@Configuration
public class AppConfig extends CommonConfig {

    @Bean
    public WebClient.Builder webClientBuilder() {
        return super.webClientBuilder();
    }
}