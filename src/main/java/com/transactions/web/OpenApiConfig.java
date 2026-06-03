package com.transactions.web;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI apiInfo() {
        return new OpenAPI().info(new Info()
                .title("API de Transacciones")
                .description("Servicio REST de transacciones en memoria: alta/actualización "
                        + "por id, listado por tipo y suma transitiva por parent_id.")
                .version("v1"));
    }
}
