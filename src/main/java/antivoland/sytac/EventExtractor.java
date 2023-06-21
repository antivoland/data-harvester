package antivoland.sytac;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
class EventExtractor {
    private final PlatformWorkerFactory factory;

    EventExtractor(PlatformWorkerFactory factory) {
        this.factory = factory;
    }

    void extract(List<String> platforms, EventHandler handler, long timeoutMillis) {
        final var timer = new Thread(() -> {
            try {
                Thread.sleep(timeoutMillis);
                log.info("Timeout exceeded");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        var workers = factory.newWorkers(platforms, wrapHandler(handler, timer));
        workers.forEach(PlatformWorker::run);
        timer.start();
        try {
            timer.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        workers.forEach(PlatformWorker::close);
    }

    private static EventHandler wrapHandler(EventHandler handler, Thread timer) {
        final var sytacOccurrences = new AtomicInteger();
        return event -> {
            if (event.getPayload() == null) return; // skip malformed events
            handler.handle(event);
            if (event.getPayload().isSytacUser() && sytacOccurrences.incrementAndGet() >= 3) {
                timer.interrupt();
                log.info("Sytac user occurred {} times", sytacOccurrences.get());
            }
        };
    }
}