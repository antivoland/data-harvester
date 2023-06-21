package antivoland.sytac;

import java.util.List;

class PlatformWorkerFactory {
    private final ClientFactory factory;

    PlatformWorkerFactory(ClientFactory factory) {
        this.factory = factory;
    }

    PlatformWorker newWorker(String platform, EventHandler handler) {
        return new PlatformWorker(platform, factory, handler);
    }

    List<PlatformWorker> newWorkers(List<String> platforms, EventHandler handler) {
        return platforms.stream().map(platform -> newWorker(platform, handler)).toList();
    }
}