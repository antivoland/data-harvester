package antivoland.sytac;

import com.launchdarkly.eventsource.ConnectStrategy;
import com.launchdarkly.eventsource.EventSource;

import java.io.Closeable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class DataHarvester implements Runnable, Closeable {
    private final List<EventProcessor> processors = new ArrayList<>();
    private final Thread timeout;
    private final AtomicInteger sytacOccurrences = new AtomicInteger();
    private final AtomicBoolean closed = new AtomicBoolean();

    public DataHarvester(List<String> platforms, String user, String pass, long timeoutMillis) {
        for (var platform : platforms) {
            processors.add(new EventProcessor(platform, this::handle, source(platform, user, pass)));
        }

        timeout = new Thread(() -> {
            try {
                Thread.sleep(timeoutMillis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            System.out.println("Timeout exceeded"); // todo: remove
            close();
        });
    }

    @Override
    public void run() {
        processors.stream().parallel().forEach(EventProcessor::run);
        timeout.start();
        try {
            timeout.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void handle(Event event) {
        System.out.println(event); // todo: remove
        if (!event.getPayload().isSytacUser()) return;
        if (sytacOccurrences.incrementAndGet() < 3) return;
        System.out.printf("Sytac occurred %s times\n", sytacOccurrences.get()); // todo: remove
        close();
    }

    @Override
    public void close() {
        if (closed.getAndSet(true)) return;
        processors.stream().parallel().forEach(EventProcessor::close);
        timeout.interrupt();
        // todo: merge and print summary
    }

    private static EventSource.Builder source(String platform, String user, String pass) {
        String url = "http://localhost:8080/" + platform;
        return new EventSource.Builder(connectStrategy(url, user, pass));
    }

    private static ConnectStrategy connectStrategy(String url, String user, String pass) {
        return ConnectStrategy
                .http(URI.create(url))
                .header("Authorization", authHeaderValue(user, pass))
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS);
    }

    private static String authHeaderValue(String user, String pass) {
        return "Basic " + Base64.getEncoder().encodeToString((user + ":" + pass).getBytes());
    }

    public static void main(String[] args) {
        String user = args[0];
        String pass = args[1];
        try (var harvester = new DataHarvester(List.of("sytflix", "sytazon", "sysney"), user, pass, 20000)) {
            harvester.run();
        }
    }
}