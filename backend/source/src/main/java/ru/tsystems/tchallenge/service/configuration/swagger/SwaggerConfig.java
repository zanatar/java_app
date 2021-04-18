package ru.tsystems.tchallenge.service.configuration.swagger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import ru.tsystems.tchallenge.service.domain.account.Account;
import ru.tsystems.tchallenge.service.domain.account.AccountSystemManager;
import ru.tsystems.tchallenge.service.security.token.SecurityToken;
import ru.tsystems.tchallenge.service.security.token.TokenManager;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import static com.google.common.collect.Lists.newArrayList;

@Configuration
@EnableSwagger2
@Profile("dev")
public class SwaggerConfig {

    private final AccountSystemManager accountSystemManager;
    private final TokenManager tokenManager;

    @Autowired
    public SwaggerConfig(AccountSystemManager accountSystemManager, TokenManager tokenManager) {
        this.accountSystemManager = accountSystemManager;
        this.tokenManager = tokenManager;
    }


    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage("ru.tsystems.tchallenge.service"))
                .paths(PathSelectors.any())
                .build()
                .tags(
                        new Tag("Event management", "Event CRUD operations"),
                        new Tag("Participant account operations",
                                "Operations to retrieve/update your account"),
                        new Tag("Account management", "Account CRUD operations. Only for admin"),
                        new Tag("Maturity", "Retrieve available maturities"),
                        new Tag("Problem management", "Problem  CRUD operations"),
                        new Tag("Specializations", "Retrieve list of specializations"),
                        new Tag("Event statistics", "Retrieve event statistics"),
                        new Tag("User statistics", "Retrieve user statistics for all events"),
                        new Tag("Workbooks management", "Create/modify operations for workbook"),
                        new Tag("Sign up", "Participant registration operation"),
                        new Tag("Sign in", "Create/read/delete token operations"),
                        new Tag("Voucher", "Creating voucher operation"),
                        new Tag("Tags", "Create/read available tags"),
                        new Tag("Event series", "Create/read/update event series")
                )
                .apiInfo(apiInfo())
                .securityContexts(Collections.singletonList(securityContext()))
                .securitySchemes(Collections.singletonList(apiKey()));
    }


    private ApiInfo apiInfo() {
        final String headerPrefix = "BEARER ";
        Account participant = accountSystemManager.findByEmail("user@user.com");
        Duration tokenExpiration = Duration.ofDays(10050);
        SecurityToken participantToken = tokenManager.create(participant.getId(), tokenExpiration);

        Account coworker = accountSystemManager.findByEmail("user2@user.com");
        SecurityToken coworkerToken = tokenManager.create(coworker.getId(), tokenExpiration);

        Account robot = accountSystemManager.findByEmail("robot@robot.com");
        SecurityToken robotToken= tokenManager.create(robot.getId(), tokenExpiration);

        StringBuilder descr = new StringBuilder();
        descr.append("You can use following predefined accounts:\n  ");
        descr.append("User Category |         User Roles         |  Authorization Token\n");
        descr.append("--------------|----------------------------|----------------------\n");
        descr.append(" Participant  |         Participant        |  ")
                .append(headerPrefix)
                .append(participantToken.getPayload())
                .append(" \n");
        descr.append(" Coworker     | Admin, Moderator, Reviewer | ")
                .append(headerPrefix)
                .append(coworkerToken.getPayload())
                .append(" \n");
        descr.append(" Robot        |          Robot             | ")
                .append(headerPrefix).
                append(robotToken.getPayload())
                .append(" \n\n");
        descr.append("#### How to use?\n  ");
        descr.append("1. Click authorize and paste one of tokens above\n");
        descr.append("2. Execute requests");

        return new ApiInfoBuilder()
                .title("T-Challenge Service REST")
                .description(descr.toString())
                .build();
    }



    private ApiKey apiKey() {
        return new ApiKey(
                "bearer",
                "Authorization",
                "header");
    }


    private SecurityContext securityContext() {
        Predicate<String> paths = PathSelectors
                .ant("/security/registrations/*")
                .or(PathSelectors.ant("/security/tokens/*"))
                .negate();
        return SecurityContext.builder().securityReferences(defaultAuth())
                .forPaths(paths::test)
                .build();
    }

    List<SecurityReference> defaultAuth() {
        AuthorizationScope authorizationScope
                = new AuthorizationScope("global", "access everywhere");
        AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
        authorizationScopes[0] = authorizationScope;
        return newArrayList(
                new SecurityReference("bearer", authorizationScopes));

    }
}
