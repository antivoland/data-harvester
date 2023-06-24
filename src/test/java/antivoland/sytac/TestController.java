package antivoland.sytac;

import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
class TestController {
    @GetMapping(path = "/sytflix", produces = "text/event-stream")
    public Flux<ServerSentEvent<String>> sytflix() {
        return Flux.fromIterable(TestEventSets.sseEvents("sytflix"));
    }

    @GetMapping(path = "/sytazon", produces = "text/event-stream")
    public Flux<ServerSentEvent<String>> sytazon() {
        return Flux.fromIterable(TestEventSets.sseEvents("sytazon"));
    }

    @GetMapping(path = "/sysney", produces = "text/event-stream")
    public Flux<ServerSentEvent<String>> sysney() {
        return Flux.fromIterable(TestEventSets.sseEvents("sysney"));
    }
}