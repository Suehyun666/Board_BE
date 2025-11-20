package org.board.board_be.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.ExternalDocumentation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .openapi("3.1.0")
                .info(new Info()
                        .title("Board API")
                        .description("""
                                ## 게시판 CRUD API 문서

                                이 API는 게시글과 댓글을 관리하는 RESTful API입니다.

                                ### 주요 기능
                                - 게시글 CRUD (생성, 조회, 수정, 삭제)
                                - 댓글 및 대댓글 CRUD
                                - 파일 업로드 (이미지, 문서 등)
                                - 페이징 및 검색

                                ### 인증
                                현재는 개발 단계로 인증이 비활성화되어 있습니다.
                                추후 JWT 기반 인증이 추가될 예정입니다.

                                ### API 서버
                                - 개발 서버: http://localhost:8080
                                - 운영 서버: https://api.moodie.shop
                                """)
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Board Team")
                                .email("gptclass14@gmail.com")
                                .url("https://api.moodie.shop"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .externalDocs(new ExternalDocumentation()
                        .description("GitHub Repository")
                        .url("https://github.com/Suehyun666/Board_BE"))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("개발 서버 - 로컬 환경"),
                        new Server()
                                .url("https://api.moodie.shop")
                                .description("운영 서버 - Production")
                ));
    }
}
