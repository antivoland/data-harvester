package antivoland.sytac;

import com.launchdarkly.eventsource.ConnectStrategy;
import com.launchdarkly.eventsource.EventSource;
import com.launchdarkly.eventsource.StreamException;

import java.net.URI;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

public class DataHarvester {
    public static void main(String[] args) throws StreamException {
        String user = args[0];
        String pass = args[1];
        var builder = new EventSource.Builder(connectStrategy("http://localhost:8080/sytflix", user, pass));
        try (var source = builder.build()) {
            source.start();
            source.messages().forEach(System.out::println);
        }
    }

    static ConnectStrategy connectStrategy(String url, String user, String pass) {
        return ConnectStrategy
                .http(URI.create(url))
                .header("Authorization", authHeaderValue(user, pass))
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS);
    }

    static String authHeaderValue(String user, String pass) {
        return "Basic " + Base64.getEncoder().encodeToString((user + ":" + pass).getBytes());
    }
}