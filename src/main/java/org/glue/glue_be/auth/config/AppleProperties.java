package org.glue.glue_be.auth.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "apple")
public class AppleProperties {

    private Auth auth = new Auth();
    private String redirectUri; // TODO: 현재 미사용 => 추후 상황보고 삭제
    private String iss;
    private String aud;
    private String teamId;
    private String grantType;
    private Key key = new Key();

    @Getter
    @Setter
    public static class Auth {
        private String tokenUrl;
        private String publicKeyUrl;
    }

    @Getter
    @Setter
    public static class Key {
        private String id;
        private String path;
    }
}
