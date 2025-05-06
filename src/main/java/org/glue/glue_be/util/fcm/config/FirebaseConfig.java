package org.glue.glue_be.util.fcm.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
@Slf4j
public class FirebaseConfig {

    @PostConstruct
    public void init() {
        try {
            InputStream serviceAccount = new ClassPathResource("firebase/serviceAccountKey.json").getInputStream();
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials( GoogleCredentials.fromStream(serviceAccount))
                    .build();

            if (FirebaseApp.getApps().isEmpty()){
                FirebaseApp.initializeApp(options);
                log.info("Firebase 초기화에 성공했습니다.");
            }
        } catch (IOException e) {
            log.error("Firebase 초기화에 실패했습니다.", e);
        }
    }
}
