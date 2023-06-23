package antivoland.sytac;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.Disposable;

import java.util.concurrent.atomic.AtomicReference;

@Slf4j
class PlatformWorker {
    private static final ParameterizedTypeReference<ServerSentEvent<String>> TYPE_REF = new ParameterizedTypeReference<>() {};
    private static final ObjectMapper MAPPER = JsonMapper.builder().addModule(new JavaTimeModule()).build();

    interface Listener {
        void onEvent(Event event);

        void onSytacUserEvent();

        void onSuccessfulStreamingEvent(Event.User user);

        void onError(Throwable error);
    }

    private final String platform;
    private final ClientFactory clientFactory;
    private final Listener listener;
    private Disposable client;

    PlatformWorker(String platform, ClientFactory clientFactory, Listener listener) {
        this.platform = platform;
        this.clientFactory = clientFactory;
        this.listener = listener;
    }

    synchronized void start() {
        if (client != null) return;
        final var lastEvent = new AtomicReference<Event>();
        client = clientFactory
                .spec(platform)
                .bodyToFlux(TYPE_REF)
                .map(this::extractEvent)
                .doOnNext(event -> {
                    listener.onEvent(event);
                    if (event.isSytacUser()) {
                        listener.onSytacUserEvent();
                    }
                    if (Event.isSuccessfulStreamingEvent(lastEvent.getAndSet(event), event)) {
                        listener.onSuccessfulStreamingEvent(event.payload.user);
                    }
                })
                .doOnError(listener::onError)
                .onErrorComplete()
                .subscribe();
    }

    synchronized void stop() {
        if (client == null) return;
        client.dispose();
        client = null;
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
            log.warn("Failed to parse event data: {}", e.getMessage());
            return null;
        }
    }
}