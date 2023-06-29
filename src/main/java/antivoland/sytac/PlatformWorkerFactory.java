package antivoland.sytac;

import java.util.List;

class PlatformWorkerFactory {
    private final ClientFactory clientFactory;

    PlatformWorkerFactory(ClientFactory clientFactory) {
        this.clientFactory = clientFactory;
    }

    PlatformWorker newWorker(String platform, PlatformWorker.Listener listener) {
        return new PlatformWorker(clientFactory.spec(platform), new EventMapper(platform), listener);
    }

    List<PlatformWorker> newWorkers(List<String> platforms, PlatformWorker.Listener listener) {
        return platforms.stream().map(platform -> newWorker(platform, listener)).toList();
    }
}