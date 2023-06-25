package antivoland.sytac;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.ServerSentEvent;

@Slf4j
class EventMapper {
    private static final ObjectMapper MAPPER = JsonMapper.builder().addModule(new JavaTimeModule()).build();

    private final String platform;

    EventMapper(String platform) {
        this.platform = platform;
    }

    Event mapEvent(ServerSentEvent<String> sseEvent) {
        return Event
                .builder()
                .id(sseEvent.id())
                .name(sseEvent.event())
                .platform(platform)
                .payload(parseEventData(sseEvent.data()))
                .build();
    }

    private static Event.Payload parseEventData(String sseEventData) {
        try {
            return MAPPER.readValue(sseEventData, Event.Payload.class);
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse event data: {}", e.getMessage());
            return null;
        }
    }
}