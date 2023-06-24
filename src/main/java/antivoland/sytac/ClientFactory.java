package antivoland.sytac;

import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Base64;

class ClientFactory {
    private final String uri;
    private final String authHeaderValue;

    ClientFactory(final String uri, final String user, final String pass) {
        this.uri = uri;
        this.authHeaderValue = authHeaderValue(user, pass);
    }

    WebClient.ResponseSpec spec(final String platform) {
        return WebClient.create(uri).get().uri(platform).header(HttpHeaders.AUTHORIZATION, authHeaderValue).retrieve();
    }

    private static String authHeaderValue(String user, String pass) {
        return "Basic " + Base64.getEncoder().encodeToString((user + ":" + pass).getBytes());
    }
}