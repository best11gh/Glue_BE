package org.glue.glue_be.common.config;

import org.springframework.context.annotation.*;
import org.springframework.http.*;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class SocialConfig {
    @Bean
    public WebClient appleWebClient(WebClient.Builder builder, AppleProperties appleProperties) {
        return builder
                .baseUrl(appleProperties.getAuth().getTokenUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .build();
    }
}
