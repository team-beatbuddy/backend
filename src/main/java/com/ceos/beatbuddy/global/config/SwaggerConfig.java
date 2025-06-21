package com.ceos.beatbuddy.global.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import java.util.List;
import java.util.Map;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
        servers = {
                @Server(url = "https://api.beatbuddy.world"),
                @Server(url = "http://localhost:8080") // 👈 로컬 서버 추가
        }
)
@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(apiInfo());
    }

    @Bean
    public GroupedOpenApi defaultApi() {
        return GroupedOpenApi.builder()
                .group("default")
                .pathsToMatch("/**")
                .pathsToExclude("/login")  // /login 경로는 제외
                .addOpenApiCustomizer(openAPI -> {
                    SecurityScheme accessScheme = new SecurityScheme()
                            .name("access")
                            .type(SecurityScheme.Type.APIKEY)
                            .in(SecurityScheme.In.HEADER)
                            .bearerFormat("JWT");
                    openAPI.getComponents().addSecuritySchemes("accessScheme", accessScheme);

                    SecurityRequirement securityRequirement = new SecurityRequirement()
                            .addList("accessScheme");

                    openAPI.addSecurityItem(securityRequirement);

                    openAPI.getPaths().entrySet().stream()
                            .filter(entry -> entry.getKey().equals("/reissue"))
                            .forEach(entry -> entry.getValue().readOperations()
                                    .forEach(operation -> operation.setSecurity(List.of())));
                })
                .build();
    }


    private Info apiInfo() {
        return new Info()
                .title("BeatBuddy API") // API의 제목
                .description("BeatBuddy API Document") // API에 대한 설명
                .version("1.0.0"); // API의 버전
    }

    @Bean
    public GroupedOpenApi loginApi() {
        return GroupedOpenApi.builder()
                .group("login")
                .pathsToMatch("/login", "/oauth2/authorization/kakao", "/oauth2/authorization/google")
                .addOpenApiCustomizer(openApi -> {

                    // 1. Kakao OAuth2 SecurityScheme
                    SecurityScheme kakaoScheme = new SecurityScheme()
                            .type(SecurityScheme.Type.OAUTH2)
                            .flows(new OAuthFlows()
                                    .authorizationCode(new OAuthFlow()
                                            .authorizationUrl("https://kauth.kakao.com/oauth/authorize")
                                            .tokenUrl("https://kauth.kakao.com/oauth/token")
                                    ));

                    // 2. Google OAuth2 SecurityScheme
                    SecurityScheme googleScheme = new SecurityScheme()
                            .type(SecurityScheme.Type.OAUTH2)
                            .flows(new OAuthFlows()
                                    .authorizationCode(new OAuthFlow()
                                            .authorizationUrl("https://accounts.google.com/o/oauth2/v2/auth")
                                            .tokenUrl("https://oauth2.googleapis.com/token")
                                    ));

                    openApi.components(new Components()
                            .addSecuritySchemes("kakaoOAuth", kakaoScheme)
                            .addSecuritySchemes("googleOAuth", googleScheme));

                    openApi.path("/oauth2/authorization/kakao", createOAuthPathItem("kakao", "카카오", "kakaoOAuth"));
                    openApi.path("/oauth2/authorization/google", createOAuthPathItem("google", "구글", "googleOAuth"));
                }).build();
    }


    private io.swagger.v3.oas.models.PathItem createOAuthPathItem(String provider, String displayName, String securitySchemeName) {
        return new io.swagger.v3.oas.models.PathItem()
                .get(new io.swagger.v3.oas.models.Operation()
                        .summary(displayName + " 로그인")
                        .description(displayName + " 로그인을 통해 회원가입을 진행합니다.")
                        .responses(new ApiResponses()
                                .addApiResponse("200", new ApiResponse()
                                        .description("로그인 성공 시 사용자 정보를 반환합니다.")
                                        .headers(Map.of(
                                                "access", new Header().description("Access Token").schema(new StringSchema()),
                                                "Set-Cookie", new Header().description("Refresh Token 쿠키").schema(new StringSchema())
                                        ))
                                        .content(new Content().addMediaType("application/json",
                                                new MediaType().schema(new Schema<>()
                                                        .addProperty("memberId", new Schema<Long>().type("integer").description("회원 식별자"))
                                                        .addProperty("loginId", new Schema<String>().type("string").description("로그인 ID"))
                                                        .addProperty("name", new Schema<String>().type("string").description("유저 이름"))
                                                ))))
                                .addApiResponse("401", new ApiResponse().description("로그인 실패"))
                        )
                        .security(List.of(new SecurityRequirement().addList(securitySchemeName)))
                );
    }
}
