package antivoland.sytac;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.Disposable;

import java.io.Closeable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

@Slf4j
class PlatformWorker implements Runnable, Closeable {
    private static final ParameterizedTypeReference<ServerSentEvent<String>> TYPE_REF = new ParameterizedTypeReference<>() {};
    private static final ObjectMapper MAPPER = JsonMapper.builder().addModule(new JavaTimeModule()).build();

    private final String platform;
    private final ClientFactory clientFactory;
    private final Consumer<Event> eventHandler;
    private final Consumer<Throwable> errorHandler;
    private Disposable client;
    private final Lock lock = new ReentrantLock();
    private boolean closed;

    PlatformWorker(String platform,
                   ClientFactory clientFactory,
                   Consumer<Event> eventHandler,
                   Consumer<Throwable> errorHandler) {
        this.platform = platform;
        this.clientFactory = clientFactory;
        this.eventHandler = eventHandler;
        this.errorHandler = errorHandler;
    }

    @Override
    public void run() {
        lock.lock();
        try {
            if (closed) return;
            if (client != null) return;
            client = clientFactory
                    .spec(platform)
                    .bodyToFlux(TYPE_REF)
                    .map(this::extractEvent)
                    .doOnError(Throwable.class, errorHandler)
                    .subscribe(eventHandler);
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

    private Event extractEvent(ServerSentEvent<String> event) {
        return Event
                .builder()
                .id(event.id())
                .name(event.event())
                .platform(platform)
                .payload(parseEventData(event.data()))
                .build();
    }

    private static Event.Payload parseEventData(String data) {
        try {
            return MAPPER.readValue(data, Event.Payload.class);
        } catch (JsonProcessingException e) {
            log.warn("Unable to process event data", e);
            return null;
        }
    }
}