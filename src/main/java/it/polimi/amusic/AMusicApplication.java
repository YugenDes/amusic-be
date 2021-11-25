package it.polimi.amusic;

import com.google.cloud.firestore.Firestore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gcp.data.firestore.repository.config.EnableReactiveFirestoreRepositories;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableReactiveFirestoreRepositories
@Slf4j
public class AMusicApplication {

    @Autowired
    private Firestore firestore;

    public static void main(String[] args) {
        SpringApplication.run(AMusicApplication.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner() {
        return args -> log.info("{} connesso a firebase", firestore.getOptions().getProjectId());
    }

}
