# Data Harvester

[![Build Status](https://github.com/antivoland/sytac-test/workflows/build/badge.svg)](https://github.com/antivoland/sytac-test/actions/workflows/build.yml)

## About

The full problem is described [here](devcase-streaming-readme-main/README.md). We need to deal with [server sent events](https://en.wikipedia.org/wiki/Server-sent_events) produced by the [streaming platform](devcase-streaming-readme-main/StreamingPlatform.md).

I've decided to move on with a spring module using `io.projectreactor` [implementation](https://projectreactor.io/docs/core/release/reference/index.html) under the hood, because I wanted to end up with nice integration tests.

## Usage

Run the streaming platform as explained in the last section of the corresponding [document](devcase-streaming-readme-main/StreamingPlatform.md):

> For Intel/AMD x64 based CPUs:
> 
> ```shell
> docker run -p 8080:8080 sytacdocker/video-stream-server:latest
> ```
> 
> For Arm based CPUs (Apple with M1/M2 chip):
> 
> ```shell
> docker run -p 8080:8080 sytacdocker/video-stream-server-arm:latest
> ```

Then run the data harvester as follows, for instance:

```shell
mvn spring-boot:run -Dspring-boot.run.arguments="sytac 4p9g-Dv7T-u8fe-iz6y-SRW2"
```

The aggregation result serialized in JSON will be printed to standard output.