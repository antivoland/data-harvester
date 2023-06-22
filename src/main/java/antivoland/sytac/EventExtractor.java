package antivoland.sytac;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
class EventExtractor {
    private final PlatformWorkerFactory workerFactory;

    EventExtractor(PlatformWorkerFactory workerFactory) {
        this.workerFactory = workerFactory;
    }

    AggregatedView extract(List<String> platforms, long timeoutMillis) {
        long startMillis = System.currentTimeMillis();
        final var timer = new Thread(() -> {
            try {
                Thread.sleep(timeoutMillis);
                log.info("Timeout exceeded");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        timer.start();

        var view = new AggregatedView();
        var workers = workerFactory.newWorkers(platforms, listener(view, timer));
        workers.forEach(PlatformWorker::run);

        try {
            timer.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        workers.forEach(PlatformWorker::close);
        view.runtimeDurationMillis = System.currentTimeMillis() - startMillis;
        return view;
    }

    private static PlatformWorker.Listener listener(AggregatedView view, Thread timer) {
        final var sytacOccurrences = new AtomicInteger();
        return new PlatformWorker.Listener() {
            @Override
            public void onEvent(Event event) {
                view.register(event);
                if (event.payload != null && event.payload.isSytacUser() && sytacOccurrences.incrementAndGet() >= 3) {
                    log.info("Sytac user occurred {} times", sytacOccurrences.get());
                    timer.interrupt();
                }
            }

            @Override
            public void onError(Throwable error) {
                log.error("Failed to process events", error);
                timer.interrupt();
            }

            @Override
            public void onSuccessfulStreamingEvent() {
                view.incrementSuccessfulStreamingEvents();
            }
        };
    }
}