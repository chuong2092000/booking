package project.paymentservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import project.commondto.dto.uri.CommonUri;
import project.commonutils.config.InternalHeaderAuthFilter;


@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        InternalHeaderAuthFilter internalHeaderAuthFilter = new InternalHeaderAuthFilter();
        httpSecurity.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorizationManagerRequestMatcherRegistry -> authorizationManagerRequestMatcherRegistry
                        .requestMatchers(CommonUri.VERSION + CommonUri.PUBLIC + "/**",
                                CommonUri.VERSION + CommonUri.INTERNAL + "/**",
                                "/actuator/health",
                                "/actuator/health/**",
                                "/actuator/info","/actuator/refresh").permitAll()
                        .anyRequest()
                        .authenticated())
                .addFilterBefore(internalHeaderAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return httpSecurity.build();
    }
}