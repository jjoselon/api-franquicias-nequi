package co.com.nequi.api.router;

import co.com.nequi.api.handler.ProductHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class ProductRouter {

    @Bean
    public RouterFunction<ServerResponse> productRoutes(ProductHandler handler) {
        return RouterFunctions.route()
                .POST("/products/add", handler::addProductToSucursal)
                .PUT("/products/{productId}/stock", handler::updateStock)
                .PUT("/products/{productId}", handler::updateProductName)
                .DELETE("/products/{productId}", handler::deleteProductFromSucursal)
                .GET("/franchises/{franchiseId}/max-stock-products", handler::getMaxStockProductBySucursal)
                .build();
    }

}
