package antivoland.sytac;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.Disposable;

import java.io.Closeable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
class PlatformWorker implements Runnable, Closeable {
    private static final ParameterizedTypeReference<ServerSentEvent<String>> TYPE_REF = new ParameterizedTypeReference<>() {};
    private static final ObjectMapper MAPPER = JsonMapper.builder().build();

    private final String platform;
    private final ClientFactory factory;
    private final EventHandler handler;
    private Disposable client;
    private final Lock lock = new ReentrantLock();
    private boolean closed;

    PlatformWorker(String platform, ClientFactory factory, EventHandler handler) {
        this.platform = platform;
        this.factory = factory;
        this.handler = handler;
    }

    @Override
    public void run() {
        lock.lock();
        try {
            if (closed) return;
            if (client != null) return;
            client = factory.spec(platform).bodyToFlux(TYPE_REF).subscribe(this::handle);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void close() {
        lock.lock();
        try {
            if (closed) return;
            if (client == null) return;
            client.dispose();
            closed = true;
        } finally {
            lock.unlock();
        }
    }

    private void handle(ServerSentEvent<String> event) {
        handler.handle(extract(event));

    }

    private Event extract(ServerSentEvent<String> event) {
        return Event
                .builder()
                .id(event.id())
                .name(event.event())
                .platform(platform)
                .payload(parse(event.data()))
                .build();
    }

    private static Event.Payload parse(String data) {
        try {
            return MAPPER.readValue(data, Event.Payload.class);
        } catch (JsonProcessingException e) {
            log.warn("Unable to process event data", e);
            return null;
        }
    }
}