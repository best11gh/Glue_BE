package org.glue.glue_be.setting;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.*;
import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.*;


@Configuration
@RequiredArgsConstructor
public class SwaggerConfig {

    private final SwaggerServerProperties serverProperties;

    @Bean
    public OpenAPI openAPI() {
        // API 기본 정보 설정
        Info info = new Info()
                .title("글루 API Document")
                .version("1.0");

        // JWT 인증 방식 설정
        String jwtScheme = "jwtAuth";
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwtScheme);
        Components components = new Components()
                .addSecuritySchemes(jwtScheme, new SecurityScheme()
                        .name("Authorization")
                        .type(SecurityScheme.Type.HTTP)
                        .in(SecurityScheme.In.HEADER)
                        .scheme("Bearer")
                        .bearerFormat("JWT"));

        // Swagger UI 설정 및 보안 추가
        return new OpenAPI()
                .components(components)
                .info(info)
                .addSecurityItem(securityRequirement)
                .servers(serverProperties.getServerUrls());
    }
}
