package com.nodueplus.platform;

import java.io.InputStream;

import org.springframework.context.annotation.Configuration;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions; // <--- UPDATED: Changed 'javax' to 'jakarta'

import jakarta.annotation.PostConstruct;

@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void init() {
        try {
            InputStream serviceAccount = getClass().getClassLoader().getResourceAsStream("serviceAccountKey.json");
            
            if (serviceAccount == null) {
                System.out.println("❌ ERROR: serviceAccountKey.json NOT FOUND!");
                return;
            }

            FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();
                
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                System.out.println("✅ FIREBASE CONNECTED SUCCESSFULLY");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}