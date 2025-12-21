package kitae.spring.health.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@EnableWebMvc
public class SwaggerConfig {

    public static final String BEARER_KEY = "BearerAuth";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .components(new Components())
            .info(apiInfo())
            // JWT 보안 스키마 추가
            .addSecurityItem(new SecurityRequirement().addList(BEARER_KEY))
            .components(new io.swagger.v3.oas.models.Components()
                .addSecuritySchemes(BEARER_KEY,
                    new SecurityScheme()
                        .name(BEARER_KEY)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("JWT Bearer 인증을 위한 헤더 입력 (예: 'Bearer {token}')")));
    }

    private Info apiInfo() {
        return new Info()
            .title("Health Care API") // API의 제목
            .description("Swagger UI") // API에 대한 설명
            .version("1.0.0"); // API의 버전
    }
}