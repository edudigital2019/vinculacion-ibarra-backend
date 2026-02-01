package com.app.municipio.application.config.swagger;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.http.HttpHeaders;

@OpenAPIDefinition(
        info = @Info(
                title = "API REST - Aplicación para el Municipio de Ibarra",
                version = "1.0.0",
                description = "API REST para la aplicación del Municipio de Ibarra, desarrollada para promocionar los emprendedores locales",
                contact = @Contact(
                        name = "Justin Moreira",
                        email = "justinmoreiragarcia@gmail.com"
                ),
                license = @io.swagger.v3.oas.annotations.info.License(
                        name = "Apache 2.0",
                        url = "https://www.apache.org/licenses/LICENSE-2.0.html"
                )
        ),
        servers = {
                @Server(
                        description = "development",
                        url = "/"
                ),
                @Server(
                        description = "localhost",
                        url = "http://localhost:8080"
                )

        },
        security = @SecurityRequirement(
                name = "Security Token"
        )

)
@SecurityScheme(
        name = "Security Token",
        description = "Access token for the API",
        type = SecuritySchemeType.HTTP,
        paramName = HttpHeaders.AUTHORIZATION,
        in = SecuritySchemeIn.HEADER,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class SwaggerConfig {
}
