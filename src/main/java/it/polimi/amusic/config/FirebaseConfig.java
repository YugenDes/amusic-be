package it.polimi.amusic.config;


import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;

import java.io.ByteArrayInputStream;
import java.io.IOException;

@Configuration
public class FirebaseConfig {

    //Account service di google firebase
    @Value("${spring.cloud.gcp.credentials.location}")
    private Resource firebaseCredentials;

    @Primary
    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        if (FirebaseApp.getApps().isEmpty()) {
            final FirebaseOptions firebaseOption = getFirebaseOption();
            FirebaseApp.initializeApp(firebaseOption);
        }
        return FirebaseApp.getInstance();
    }

    @Bean
    public FirebaseAuth firebaseAuth() throws IOException {
        return FirebaseAuth.getInstance(firebaseApp());
    }

    @Bean
    public FirebaseOptions getFirebaseOption() throws IOException {
        return FirebaseOptions.builder()
                .setCredentials(getGoogleCredentials())
                .build();
    }

    @Bean
    public GoogleCredentials getGoogleCredentials() throws IOException {
        return GoogleCredentials.fromStream(new ByteArrayInputStream(firebaseCredentials.getInputStream().readAllBytes()));
    }

}
