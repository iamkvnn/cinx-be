package com.cinx.notification.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import java.io.InputStream;

@Configuration
@Slf4j
public class FirebaseConfig {

    @PostConstruct
    public void initialize() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                // To run successfully locally without a file, you'd load the JSON key from resource.
                // Assuming it's in resources/firebase-service-account.json
                // InputStream serviceAccount = getClass().getClassLoader().getResourceAsStream("firebase-service-account.json");
                
                // For now, attempting application default credentials or minimal setup
                // In production, GOOGLE_APPLICATION_CREDENTIALS environment variable should be set
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.getApplicationDefault())
                        .build();

                FirebaseApp.initializeApp(options);
                log.info("Firebase application has been initialized");
            }
        } catch (Exception e) {
            log.warn("Firebase initialization failed. Push notifications might not work. Error: {}", e.getMessage());
        }
    }
}