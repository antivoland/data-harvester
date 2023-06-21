package antivoland.sytac;

import java.util.List;
import java.util.function.Consumer;

class PlatformWorkerFactory {
    private final ClientFactory clientFactory;

    PlatformWorkerFactory(ClientFactory clientFactory) {
        this.clientFactory = clientFactory;
    }

    PlatformWorker newWorker(String platform,
                             Consumer<Event> eventHandler,
                             Consumer<Throwable> errorHandler) {
        return new PlatformWorker(platform, clientFactory, eventHandler, errorHandler);
    }

    List<PlatformWorker> newWorkers(List<String> platforms,
                                    Consumer<Event> eventHandler,
                                    Consumer<Throwable> errorHandler) {
        return platforms.stream().map(platform -> newWorker(platform, eventHandler, errorHandler)).toList();
    }
}