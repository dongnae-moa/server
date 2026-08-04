package zaman.dongnaemoa.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String BEARER_AUTH = "bearerAuth";

    @Bean
    public OpenAPI openApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("동네모아 API")
                        .description("GPS 기반 동네 인증, 퀘스트 등록/참여, 포인트 보상을 제공하는 동네 문제 해결 커뮤니티 API. "
                                + "회원가입/로그인으로 발급받은 accessToken을 우측 상단 Authorize에 "
                                + "'Bearer {accessToken}' 형태로 입력하면 인증이 필요한 API를 바로 테스트할 수 있습니다.")
                        .version("v1"))
                .components(new Components()
                        .addSecuritySchemes(BEARER_AUTH, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
