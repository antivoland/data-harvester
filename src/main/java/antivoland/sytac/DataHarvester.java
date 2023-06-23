package antivoland.sytac;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import java.util.List;

@Slf4j
@SpringBootApplication
public class DataHarvester implements CommandLineRunner {
    @Override
    public void run(String... args) {
        var clientFactory = new ClientFactory("http://localhost:8080", args[0], args[1]);
        var workerFactory = new PlatformWorkerFactory(clientFactory);
        var aggregator = new Aggregator(workerFactory);
        var view = aggregator.aggregate(List.of("sytflix", "sytazon", "sysney"), 3000);
        view.print();
    }

    public static void main(String[] args) {
        // SpringApplication.run(DataHarvester.class, args);
        new SpringApplicationBuilder(DataHarvester.class).web(WebApplicationType.NONE).run(args);
    }
}