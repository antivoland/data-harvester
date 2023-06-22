package antivoland.sytac;

import java.util.List;
import java.util.function.Consumer;

class PlatformWorkerFactory {
    private final ClientFactory clientFactory;

    PlatformWorkerFactory(ClientFactory clientFactory) {
        this.clientFactory = clientFactory;
    }

    PlatformWorker newWorker(String platform, PlatformWorker.Listener listener) {
        return new PlatformWorker(platform, clientFactory, listener);
    }

    List<PlatformWorker> newWorkers(List<String> platforms, PlatformWorker.Listener listener) {
        return platforms.stream().map(platform -> newWorker(platform, listener)).toList();
    }
}