package project.paymentservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import project.commonutils.config.CommonConfig;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.time.Duration;

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

    @Bean
    @LoadBalanced
    public WebClient.Builder webClientBuilder() {
        return super.webClientBuilder();
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) throws NoSuchAlgorithmException, KeyManagementException {
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                }
        };

        // 2. Khởi tạo SSLContext sử dụng TrustManager giả ở trên
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

        return builder
                .requestFactory(() -> {
                    // 3. Tùy biến SimpleClientHttpRequestFactory để nhét SSLContext vào
                    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory() {
                        @Override
                        protected void prepareConnection(HttpURLConnection connection, String httpMethod) throws IOException, IOException {
                            if (connection instanceof HttpsURLConnection httpsConnection) {
                                httpsConnection.setSSLSocketFactory(sslContext.getSocketFactory());
                                httpsConnection.setHostnameVerifier((hostname, session) -> true);
                            }
                            super.prepareConnection(connection, httpMethod);
                        }
                    };

                    factory.setConnectTimeout(Duration.ofSeconds(5));
                    factory.setReadTimeout(Duration.ofSeconds(5));
                    return factory;
                })
                .build();
    }
}