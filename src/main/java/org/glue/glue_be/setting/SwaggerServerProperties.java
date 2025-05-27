package org.glue.glue_be.setting;

import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
@ConfigurationProperties(prefix = "swagger")
public class SwaggerServerProperties {
    private List<Server> serverUrls;

    public void setServerUrls(List<Server> serverUrls) {
        this.serverUrls = serverUrls;
    }
}