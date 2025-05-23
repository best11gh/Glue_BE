package org.glue.glue_be;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaAuditing
public class GlueBeApplication {

	public static void main(String[] args) {
		SpringApplication.run(GlueBeApplication.class, args);
	}

}
