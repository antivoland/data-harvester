# Data Harvester

We need to deal with [server sent events](https://en.wikipedia.org/wiki/Server-sent_events) produced by the [streaming platform](devcase-streaming-readme-main/StreamingPlatform.md).

The [okhttp-eventsource](https://github.com/launchdarkly/okhttp-eventsource) library looks suitable for reading such events. It looks well written and tested.

At this stage I will pass the username and password required for authentication with the program arguments.