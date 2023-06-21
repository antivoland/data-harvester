package antivoland.sytac;

import org.springframework.web.reactive.function.client.WebClient;

import java.util.Base64;

class ClientFactory {
    private final String authHeaderValue;

    ClientFactory(final String user, final String pass) {
        authHeaderValue = authHeaderValue(user, pass);
    }

    WebClient.ResponseSpec spec(final String platform) {
        return WebClient
                .create("http://localhost:8080")
                .get()
                .uri(platform)
                .header("Authorization", authHeaderValue)
                .retrieve();
    }

    private static String authHeaderValue(String user, String pass) {
        return "Basic " + Base64.getEncoder().encodeToString((user + ":" + pass).getBytes());
    }
}