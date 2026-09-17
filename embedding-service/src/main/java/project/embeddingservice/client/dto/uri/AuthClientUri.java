package project.embeddingservice.client.dto.uri;

public interface AuthClientUri {
    String LOGIN = "/login";
    String LOGOUT = "/logout";
    String REFRESH_TOKEN = "/refresh-token";
    String REVOKE_TOKEN = "/revoke-token";
    String USER_INFO = "/user-info";
}
