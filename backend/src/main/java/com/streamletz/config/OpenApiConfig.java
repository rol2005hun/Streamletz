package com.streamletz.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for OpenAPI (Swagger) documentation.
 * 
 * <p>
 * This configuration sets up the Swagger UI and OpenAPI specification for
 * the Streamletz API, including:
 * </p>
 * <ul>
 * <li>API metadata (title, version, description)</li>
 * <li>Contact information and licensing</li>
 * <li>JWT Bearer token authentication scheme</li>
 * <li>Security requirements for protected endpoints</li>
 * </ul>
 * 
 * <p>
 * The Swagger UI can be accessed at {@code /swagger-ui.html} and the
 * OpenAPI specification at {@code /v3/api-docs}.
 * </p>
 * 
 * @author Streamletz Team
 * @version 1.0
 * @since 1.0
 */
@Configuration
public class OpenApiConfig {

        /**
         * Creates and configures the OpenAPI specification bean.
         * 
         * <p>
         * Configures the API documentation with:
         * </p>
         * <ul>
         * <li><b>API Information:</b> Title, version, description, contact details, and
         * license</li>
         * <li><b>Security:</b> JWT Bearer token authentication scheme named
         * "bearerAuth"</li>
         * <li><b>Global Security:</b> Applies the security requirement to all endpoints
         * by default</li>
         * </ul>
         * 
         * <p>
         * The JWT security scheme expects tokens in the format:
         * {@code Authorization: Bearer <token>}
         * </p>
         * 
         * @return the configured OpenAPI instance for Swagger documentation
         */
        @Bean
        public OpenAPI customOpenAPI() {
                final String securitySchemeName = "bearerAuth";

                return new OpenAPI()
                                .info(new Info()
                                                .title("Streamletz API")
                                                .version("1.0.0")
                                                .description("Music streaming API - Your sound. Your stream. Your rules.")
                                                .contact(new Contact()
                                                                .name("Streamletz Team")
                                                                .url("https://streamletz.com"))
                                                .license(new License()
                                                                .name("MIT")
                                                                .url("https://opensource.org/licenses/MIT")))
                                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                                .components(new Components()
                                                .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                                                .name(securitySchemeName)
                                                                .type(SecurityScheme.Type.HTTP)
                                                                .scheme("bearer")
                                                                .bearerFormat("JWT")));
        }
}