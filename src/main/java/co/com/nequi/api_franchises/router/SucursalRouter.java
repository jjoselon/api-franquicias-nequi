package co.com.nequi.api_franchises.router;

import co.com.nequi.api_franchises.handler.SucursalHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class SucursalRouter {
    @Bean
    public RouterFunction<ServerResponse> sucursalRoutes(SucursalHandler handler) {
        return RouterFunctions.route()
                .POST("/sucursal", handler::createSucursal)
                .PUT("/sucursal/{sucursalId}", handler::updateSucursalName)
                .build();
    }
}
