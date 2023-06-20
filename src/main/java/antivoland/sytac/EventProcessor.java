package antivoland.sytac;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.launchdarkly.eventsource.EventSource;
import com.launchdarkly.eventsource.MessageEvent;
import com.launchdarkly.eventsource.background.BackgroundEventHandler;
import com.launchdarkly.eventsource.background.BackgroundEventSource;

import java.io.Closeable;
import java.util.function.Consumer;

class EventProcessor implements Runnable, Closeable {
    private static final ObjectMapper MAPPER = JsonMapper.builder().build();

    private final String platform;
    private final Consumer<Event> handler;
    private final BackgroundEventSource source;

    public EventProcessor(String platform, Consumer<Event> handler, EventSource.Builder source) {
        this.platform = platform;
        this.handler = handler;
        this.source = new BackgroundEventSource.Builder(new Handler(), source).build();
    }

    @Override
    public void run() {
        source.start();
    }

    @Override
    public void close() {
        source.close();
    }

    private void handle(Event event) {
        if (event.getPayload() == null) return;
        // todo: handle the event
        handler.accept(event);
    }

    private Event extract(MessageEvent event) {
        return Event
                .builder()
                .id(event.getLastEventId())
                .name(event.getEventName())
                .platform(platform)
                .payload(parse(event.getData()))
                .build();
    }

    private static Event.Payload parse(String data) {
        try {
            return MAPPER.readValue(data, Event.Payload.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace(); // todo: warning
            return null;
        }
    }

    private class Handler implements BackgroundEventHandler {
        @Override
        public void onOpen() {}

        @Override
        public void onClosed() {}

        @Override
        public void onMessage(String name, MessageEvent event) {
            handle(extract(event));
        }

        @Override
        public void onComment(String comment) {}

        @Override
        public void onError(Throwable t) {}
    }
}