package antivoland.sytac;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import java.util.List;

@SpringBootApplication
public class DataHarvester implements CommandLineRunner, EventHandler {


    @Override
    public void handle(Event event) {
        System.out.println(event); // TODO: handle the event
    }

    @Override
    public void run(String... args) {
        var clientFactory = new ClientFactory(args[0], args[1]);
        var workerFactory = new PlatformWorkerFactory(clientFactory);
        var extractor = new EventExtractor(workerFactory);
        extractor.extract(List.of("sytflix", "sytazon", "sysney"), this, 200000);
    }

    public static void main(String[] args) {
        // SpringApplication.run(DataHarvester.class, args);
        new SpringApplicationBuilder(DataHarvester.class).web(WebApplicationType.NONE).run(args);
    }
}