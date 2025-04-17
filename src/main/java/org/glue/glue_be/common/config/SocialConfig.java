package org.glue.glue_be.common.config;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class SocialConfig {
    private final RestTemplate restTemplate = new RestTemplate();

    public RestTemplate restTemplate() {
        return restTemplate;
    }
}