package com.GoPedidos.GoPedidos.Configurações;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class SwaggerConfig {

	@Bean
	public OpenAPI customOpenAPI() {
		return new OpenAPI()
				.info(new Info()
						.title("GoPedidos API")
						.version("1.0")
						.description("API para gerenciamento de pedidos e usuários"))
				.addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
				.components(new Components()
						.addSecuritySchemes("Bearer Authentication", // 🔹 O nome precisa ser o mesmo aqui
								new SecurityScheme()
										.name("Authorization")
										.type(SecurityScheme.Type.HTTP)
										.scheme("bearer")
										.bearerFormat("JWT")));
	}
}
