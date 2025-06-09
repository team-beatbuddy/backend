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
                .pathsToMatch("/login")
                .addOpenApiCustomizer(
                        openApi -> {
                            SecurityScheme oauthScheme = new SecurityScheme()
                                    .type(SecurityScheme.Type.OAUTH2)
                                    .flows(new OAuthFlows()
                                            .authorizationCode(new OAuthFlow()
                                                    .authorizationUrl("https://kauth.kakao.com/oauth/authorize")
                                                    .tokenUrl("https://kauth.kakao.com/oauth/token")
                                            ));

                            // Components에 OAuth2 보안 스키마 추가
                            openApi.components(new Components().addSecuritySchemes("oauth2", oauthScheme));

                            // SecurityRequirement 정의
                            SecurityRequirement securityRequirement = new SecurityRequirement().addList("oauth2");

                            openApi.addSecurityItem(securityRequirement)
                                    .path("/oauth2/authorization/kakao", new io.swagger.v3.oas.models.PathItem()
                                            .get(new io.swagger.v3.oas.models.Operation()
                                                    .summary("로그인 로직")
                                                    .description("카카오 로그인을 통해 회원가입을 진행합니다")
                                                    .responses(new ApiResponses()
                                                            .addApiResponse("200", new ApiResponse()
                                                                    .description("로그인 성공 시 사용자 정보들을 반환합니다"
                                                                            + "헤더에는 Access Token, 쿠키에는 Refresh Token을 담아 Response됩니다.")
                                                                    .headers(Map.of(
                                                                                    "access", new Header()
                                                                                            .description("Access Token입니다")
                                                                                            .schema(new StringSchema()),
                                                                                    "Set-Cookie", new Header()
                                                                                            .description(
                                                                                                    "Refresh Token을 포함하는 쿠키입니다")
                                                                                            .schema(new StringSchema())
                                                                            )
                                                                    )
                                                                    .content(new Content()
                                                                            .addMediaType("application/json",
                                                                                    new MediaType()
                                                                                            .schema(new Schema<>()
                                                                                                    .addProperty(
                                                                                                            "memberId",
                                                                                                            new Schema<Long>().type(
                                                                                                                            "integer")
                                                                                                                    .description(
                                                                                                                            "회원 식별자"))
                                                                                                    .addProperty(
                                                                                                            "loginId",
                                                                                                            new Schema<String>().type(
                                                                                                                            "string")
                                                                                                                    .description(
                                                                                                                            "로그인 ID"
                                                                                                                                    + "이는 어느 Oauth2를 사용해 로그인한 유저인 지를 식별하기 위한 값입니다."
                                                                                                                                    + "ex) kakao_{Oauth2_user_id}"))
                                                                                                    .addProperty("name",
                                                                                                            new Schema<String>().type(
                                                                                                                            "string")
                                                                                                                    .description(
                                                                                                                            "유저의 이름입니다"
                                                                                                                                    + "일단은 Oauth2에서 받아온 nickname을 이름으로 사용하고 있습니다"
                                                                                                                                    + "그래서 기본 닉네임이 실명이 아닌 유저는 본명이 아닐 수도 있습니다"))
                                                                                            )
                                                                            )
                                                                    )
                                                            )
                                                            .addApiResponse("401", new ApiResponse()
                                                                    .description("로그인 실패 시 에러 메시지를 반환합니다")
                                                            ))
                                            ));

                        }).build();
    }
}
