package co.com.nequi.api.handler;

import co.com.nequi.api.entity.Product;
import co.com.nequi.api.entity.Sucursal;
import co.com.nequi.api.repository.FranchiseRepository;
import co.com.nequi.api.repository.ProductRepository;
import co.com.nequi.api.repository.SucursalRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ProductHandler {

    private final ProductRepository productRepository;
    private final SucursalRepository sucursalRepository;
    private final FranchiseRepository franchiseRepository;

    public ProductHandler(ProductRepository productRepository,
                          SucursalRepository sucursalRepository,
                          FranchiseRepository franchiseRepository) {
        this.productRepository = productRepository;
        this.sucursalRepository = sucursalRepository;
        this.franchiseRepository = franchiseRepository;
    }

    public Mono<ServerResponse> addProductToSucursal(ServerRequest request) {
        return request.bodyToMono(Product.class)
                .flatMap(product -> Mono.justOrEmpty(sucursalRepository.findById(product.getSucursal().getId()))
                        .switchIfEmpty(Mono.error(new RuntimeException("Sucursal not found")))
                        .flatMap(sucursal -> {
                            product.setSucursal(sucursal);
                            return Mono.fromCallable(() -> productRepository.save(product))
                                    .subscribeOn(Schedulers.boundedElastic());
                        }))
                .flatMap(savedProduct -> ServerResponse.status(HttpStatus.CREATED).bodyValue(savedProduct))
                .onErrorResume(e -> ServerResponse.status(HttpStatus.BAD_REQUEST).bodyValue(e.getMessage()));
    }

    public Mono<ServerResponse> deleteProductFromSucursal(ServerRequest request) {
        Long productId = Long.parseLong(request.pathVariable("productId"));

        return Mono.fromCallable(() -> productRepository.findById(productId))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(optionalProduct -> optionalProduct.map(product ->
                        Mono.fromCallable(() -> {
                            productRepository.delete(product);
                            return ServerResponse.noContent().build();
                        }).subscribeOn(Schedulers.boundedElastic()).flatMap(response -> response)
                ).orElseGet(() -> ServerResponse.status(HttpStatus.NOT_FOUND)
                        .bodyValue("Product not found")))
                .onErrorResume(e -> ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .bodyValue("Error deleting product: " + e.getMessage()));
    }

    public Mono<ServerResponse> updateStock(ServerRequest request) {
        Long productId = Long.parseLong(request.pathVariable("productId"));

        return request.bodyToMono(Integer.class) // Leemos el stock directamente como un Integer
                .flatMap(newStock -> Mono.fromCallable(() -> productRepository.findById(productId))
                        .subscribeOn(Schedulers.boundedElastic())
                        .flatMap(optionalProduct -> optionalProduct.map(product ->
                                Mono.fromCallable(() -> {
                                    product.setStock(newStock);
                                    productRepository.save(product);
                                    return product;
                                }).subscribeOn(Schedulers.boundedElastic())
                        ).orElseGet(() -> Mono.error(new RuntimeException("Product not found"))))
                )
                .flatMap(updatedProduct -> ServerResponse.ok().bodyValue(updatedProduct))
                .onErrorResume(e -> ServerResponse.status(HttpStatus.BAD_REQUEST).bodyValue(e.getMessage()));
    }

    public Mono<ServerResponse> updateProductName(ServerRequest request) {
        Long productId = Long.parseLong(request.pathVariable("productId"));

        return request.bodyToMono(Product.class)
                .flatMap(newProductData -> Mono.fromCallable(() -> productRepository.findById(productId))
                        .subscribeOn(Schedulers.boundedElastic())
                        .flatMap(optionalProduct -> optionalProduct.map(product -> {
                            product.setNombre(newProductData.getNombre());
                            return Mono.fromCallable(() -> productRepository.save(product))
                                    .subscribeOn(Schedulers.boundedElastic());
                        }).orElseGet(() -> Mono.error(new RuntimeException("Product not found")))))
                .flatMap(updatedProduct -> ServerResponse.ok().bodyValue(updatedProduct))
                .onErrorResume(e -> ServerResponse.status(HttpStatus.BAD_REQUEST).bodyValue(e.getMessage()));
    }

    public Mono<ServerResponse> getMaxStockProductBySucursal(ServerRequest request) {
        Long franchiseId = Long.parseLong(request.pathVariable("franchiseId"));

        return Mono.fromCallable(() -> franchiseRepository.findById(franchiseId))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(optionalFranchise -> optionalFranchise.map(franchise ->
                        Mono.fromCallable(() -> sucursalRepository.findByFranchiseIdWithProducts(franchiseId))
                                .subscribeOn(Schedulers.boundedElastic())
                                .map(sucursales -> sucursales.stream()
                                        .map(this::getMaxStockProduct)
                                        .filter(Optional::isPresent)
                                        .map(Optional::get)
                                        .collect(Collectors.toList())
                                )
                ).orElseGet(() -> Mono.error(new RuntimeException("Franchise not found"))))
                .flatMap(products -> ServerResponse.ok().bodyValue(products))
                .onErrorResume(e -> ServerResponse.status(HttpStatus.BAD_REQUEST).bodyValue(e.getMessage()));
    }

    private Optional<ProductWithSucursalDTO> getMaxStockProduct(Sucursal sucursal) {
        return sucursal.getProductos().stream()
                .max((p1, p2) -> Integer.compare(p1.getStock(), p2.getStock()))
                .map(product -> new ProductWithSucursalDTO(product.getNombre(), product.getStock(), sucursal.getNombre()));
    }

    private record ProductWithSucursalDTO(String nombre, int stock, String sucursalNombre) {}
}
