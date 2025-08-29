package seasonton.youthPolicy.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI seasontonAPI() {
        Info info = new Info()
                .title("seasonton API")
                .description("seasonton API 명세서")
                .version("1.0.0");


        return new OpenAPI()
                .addServersItem(new Server().url("/"))
                .info(info);
    }
}
