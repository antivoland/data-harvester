package antivoland.sytac;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.Disposable;

import java.util.concurrent.atomic.AtomicReference;

@Slf4j
class PlatformWorker {
    private static final ParameterizedTypeReference<ServerSentEvent<String>> TYPE_REF = new ParameterizedTypeReference<>() {};

    interface Listener {
        void onEvent(Event event);

        void onSytacUserEvent();

        void onSuccessfulStreamingEvent(Event.User user);

        void onError(Throwable error);
    }

    private final WebClient.ResponseSpec spec;
    private final EventMapper mapper;
    private final Listener listener;
    private Disposable client;

    PlatformWorker(WebClient.ResponseSpec spec, EventMapper mapper, Listener listener) {
        this.spec = spec;
        this.mapper = mapper;
        this.listener = listener;
    }

    synchronized void start() {
        if (client != null) return;
        final var lastEvent = new AtomicReference<Event>();
        client = spec
                .bodyToFlux(TYPE_REF)
                .map(mapper::mapEvent)
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
}