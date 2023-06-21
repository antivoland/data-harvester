package antivoland.sytac;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

@Slf4j
class EventExtractor {
    private final PlatformWorkerFactory workerFactory;

    EventExtractor(PlatformWorkerFactory workerFactory) {
        this.workerFactory = workerFactory;
    }

    void extract(List<String> platforms, EventHandler eventHandler, long timeoutMillis) {
        final var timer = new Thread(() -> {
            try {
                Thread.sleep(timeoutMillis);
                log.info("Timeout exceeded");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        var workers = workerFactory.newWorkers(platforms, wrapEventHandler(eventHandler, timer), errorHandler(timer));
        workers.forEach(PlatformWorker::run);
        timer.start();
        try {
            timer.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        workers.forEach(PlatformWorker::close);
    }

    private static Consumer<Event> wrapEventHandler(EventHandler eventHandler, Thread timer) {
        final var sytacOccurrences = new AtomicInteger();
        return event -> {
            if (event.getPayload() == null) return; // skip malformed events
            eventHandler.handle(event);
            if (event.getPayload().isSytacUser() && sytacOccurrences.incrementAndGet() >= 3) {
                log.info("Sytac user occurred {} times", sytacOccurrences.get());
                timer.interrupt();
            }
        };
    }

    private static Consumer<Throwable> errorHandler(Thread timer) {
        return error -> {
            log.info("Failed to process events", error);
            timer.interrupt();
        };
    }
}