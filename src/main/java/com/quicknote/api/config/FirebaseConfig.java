package com.quicknote.api.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FirebaseConfig {

	@PostConstruct
	public void initializeFirebase() throws IOException {
		if (FirebaseApp.getApps().isEmpty()) {
			FirebaseOptions options = FirebaseOptions.builder()
				.setCredentials(GoogleCredentials.getApplicationDefault())
				.build();
			FirebaseApp.initializeApp(options);
		}
	}

	@Bean
	public Firestore firestore() {
		return FirestoreClient.getFirestore();
	}
}