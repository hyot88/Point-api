package com.point.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.DocExpansion;
import springfox.documentation.swagger.web.UiConfiguration;
import springfox.documentation.swagger.web.UiConfigurationBuilder;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig {

    @Bean
    public Docket restAPI() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.point"))
                .paths(PathSelectors.any())
                .build();
    }

    private ApiInfo apiInfo() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("API 보충 설명\n");
        stringBuilder.append("1. 회원 테이블이 따로 없으므로, 별다른 체크 없이 입력받은 회원번호로 CRUD 처리한다.\n");
        stringBuilder.append("2. 포인트 사용 시, 적립했던 포인트를 3곳에서 차감했다 하더라도, 내역 조회할 때는 1개만 출력되도록 처리한다.\n");
        stringBuilder.append("3. 포인트 사용 취소는 해당 포인트 ID를 알고 있어야 한다.\n");
        stringBuilder.append("4. 포인트 적립/사용 내역은 1페이지당 5개 내역을 출력하도록 한다.");

        return new ApiInfoBuilder()
                .title("Point API")
                .description(stringBuilder.toString())
                .version("1.0")
                .build();
    }

    @Bean
    UiConfiguration uiConfig() {
        return UiConfigurationBuilder.builder()
                .docExpansion(DocExpansion.LIST) // or DocExpansion.NONE or DocExpansion.FULL
                .build();
    }
}
