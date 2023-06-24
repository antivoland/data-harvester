package antivoland.sytac;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class TestListener implements PlatformWorker.Listener {
    final List<Event> events = new ArrayList<>();
    int sytacUserEvents;
    final Map<String, Integer> successfulStreamingEvents = new HashMap<>();
    int errors;

    @Override
    public synchronized void onEvent(Event event) {
        events.add(event);
    }

    @Override
    public synchronized void onSytacUserEvent() {
        ++sytacUserEvents;
    }

    @Override
    public synchronized void onSuccessfulStreamingEvent(Event.User user) {
        successfulStreamingEvents.put(user.id, successfulStreamingEvents.getOrDefault(user.id, 0) + 1);
    }

    @Override
    public synchronized void onError(Throwable error) {
        ++errors;
    }
}
