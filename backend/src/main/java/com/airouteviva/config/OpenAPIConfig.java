package com.airouteviva.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("AI-ROUTE VIVA — Spring Boot Control Plane API")
                        .version("1.0.0")
                        .description("""
                                ### Resource-Aware Local AI Infrastructure for Multimodal Viva Proctoring
                                **HackWithAMYPO 2026** | Integration of **PS1** (Low-Cost AI Request Router) and **PS5** (Viva Proctoring).
                                
                                The Spring Boot backend acts as the authoritative control plane orchestrating students, viva sessions,
                                questions, answers, and proctoring events, while delegating routing decisions to the Go router.
                                """)
                        .contact(new Contact()
                                .name("AI-ROUTE VIVA Team")
                                .url("https://github.com/Vibinchandar2299/Viva-Proctoring"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")));
    }
}
