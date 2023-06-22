package antivoland.sytac;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.scheduler.Schedulers;

import java.io.Closeable;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

@Slf4j
class PlatformWorker implements Runnable, Closeable {
    private static final ParameterizedTypeReference<ServerSentEvent<String>> TYPE_REF = new ParameterizedTypeReference<>() {};
    private static final ObjectMapper MAPPER = JsonMapper.builder().addModule(new JavaTimeModule()).build();

    interface Listener {
        void onEvent(Event event);

        void onError(Throwable error);

        void onSuccessfulStreamingEvent();
    }

    private final String platform;
    private final ClientFactory clientFactory;
    private final Listener listener;
    private Stream<Event> stream;
    private final Lock lock = new ReentrantLock();
    private boolean closed;

    PlatformWorker(String platform, ClientFactory clientFactory, Listener listener) {
        this.platform = platform;
        this.clientFactory = clientFactory;
        this.listener = listener;
    }

    @Override
    public void run() {
        lock.lock();
        try {
            if (closed) return;
            if (stream != null) return;
            stream = clientFactory
                    .spec(platform)
                    .bodyToFlux(TYPE_REF)
                    .map(this::extractEvent)
                    .filter(event -> event.payload != null)
                    .doOnError(listener::onError)
                    .subscribeOn(Schedulers.newSingle(platform + "-worker"), false)
                    .toStream();

            new Thread(() -> {
                AtomicReference<Event> lastEvent = new AtomicReference<>();
                stream.forEach(event -> {
                    listener.onEvent(event);
                    if (Event.isSuccessfulStreamingEvent(lastEvent.getAndSet(event), event)) {
                        listener.onSuccessfulStreamingEvent();
                    }
                });
            }).start();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void close() {
        lock.lock();
        try {
            if (closed) return;
            if (stream == null) return;
            stream.close();
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