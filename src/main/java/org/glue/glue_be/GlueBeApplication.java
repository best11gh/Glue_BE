package org.glue.glue_be;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling // 채팅방 폭파 스케쥴링
public class GlueBeApplication {

	public static void main(String[] args) {
		SpringApplication.run(GlueBeApplication.class, args);
	}

}
