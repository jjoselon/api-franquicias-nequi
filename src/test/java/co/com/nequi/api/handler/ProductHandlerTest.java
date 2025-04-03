package co.com.nequi.api.handler;

import co.com.nequi.api.entity.Franchise;
import co.com.nequi.api.entity.Product;
import co.com.nequi.api.entity.Sucursal;
import co.com.nequi.api.repository.FranchiseRepository;
import co.com.nequi.api.repository.ProductRepository;
import co.com.nequi.api.repository.SucursalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.only;

@SpringBootTest
public class ProductHandlerTest {

    @Mock
    private FranchiseRepository franchiseRepository;

    @Mock
    private SucursalRepository sucursalRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductHandler productHandler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(true);
    }

    @Test
    void testAddProductToSucursal_Success() {

        Long productId = 1L;
        Long sucursalId = 12L;
        Long franchiseId = 3L;

        Franchise franchise = new Franchise(franchiseId, "Droguerias Farmacolombia", Collections.emptyList());
        Sucursal existingSucursal = new Sucursal(sucursalId, "Sucursal del norte", franchise, Collections.emptyList());
        Product newProduct = new Product(productId, "Acetaminofen", 300, existingSucursal);

        ServerRequest request = mock(ServerRequest.class);

        when(request.bodyToMono(Product.class)).thenReturn(Mono.just(newProduct));
        when(sucursalRepository.findById(sucursalId)).thenReturn(Optional.of(existingSucursal));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Mono<ServerResponse> responseMono = productHandler.addProductToSucursal(request);

        StepVerifier.create(responseMono)
                .expectNextMatches(serverResponse -> {
                    assertEquals(HttpStatus.CREATED, serverResponse.statusCode());
                    return true;
                })
                .verifyComplete();

        verify(sucursalRepository, only()).findById(sucursalId);
        verify(productRepository, only()).save(newProduct);

    }

    @Test
    void testAddProductToSucursal_FailureWhenSucursalWasNotFound() {

        Long productId = 1L;
        Long sucursalId = 12L;
        Long franchiseId = 3L;

        Franchise franchise = new Franchise(franchiseId, "Droguerias Farmacolombia", Collections.emptyList());
        Sucursal existingSucursal = new Sucursal(sucursalId, "Sucursal del norte", franchise, Collections.emptyList());
        Product newProduct = new Product(productId, "Acetaminofen", 300, existingSucursal);

        ServerRequest request = mock(ServerRequest.class);

        when(request.bodyToMono(Product.class)).thenReturn(Mono.just(newProduct));
        when(sucursalRepository.findById(sucursalId)).thenReturn(Optional.empty()); // No encontró la sucursal

        Mono<ServerResponse> responseMono = productHandler.addProductToSucursal(request);

        StepVerifier.create(responseMono)
                .expectNextMatches(serverResponse -> {
                    assertEquals(HttpStatus.BAD_REQUEST, serverResponse.statusCode());
                    return true;
                })
                .verifyComplete();

        verify(sucursalRepository, only()).findById(sucursalId);
        verify(productRepository, never()).save(newProduct);
        
    }
    
    @Test
    void testDeleteProductFromSucursal_Success() {

        Long productId = 1L;
        Long sucursalId = 12L;
        Long franchiseId = 3L;

        Franchise franchise = new Franchise(franchiseId, "Droguerias Farmacolombia", Collections.emptyList());
        Sucursal existingSucursal = new Sucursal(sucursalId, "Sucursal del norte", franchise, Collections.emptyList());
        Product product = new Product(productId, "Acetaminofen", 300, existingSucursal);

        ServerRequest request = mock(ServerRequest.class);

        when(request.pathVariable("productId")).thenReturn(productId.toString());
        when(request.bodyToMono(Product.class)).thenReturn(Mono.just(product));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        //when(productRepository.delete(product)).thenReturn(any());

        Mono<ServerResponse> responseMono = productHandler.deleteProductFromSucursal(request);

        StepVerifier.create(responseMono)
                .expectNextMatches(serverResponse -> {
                    assertEquals(HttpStatus.NO_CONTENT, serverResponse.statusCode());
                    return true;
                })
                .verifyComplete();

        verify(productRepository, times(1)).findById(productId);
        verify(productRepository, times(1)).delete(product);

    }

    @Test
    void testDeleteProductFromSucursal_FailureWhenProductWasNotFound() {

        Long productId = 1L;
        Long sucursalId = 12L;
        Long franchiseId = 3L;

        Franchise franchise = new Franchise(franchiseId, "Droguerias Farmacolombia", Collections.emptyList());
        Sucursal existingSucursal = new Sucursal(sucursalId, "Sucursal del norte", franchise, Collections.emptyList());
        Product product = new Product(productId, "Acetaminofen", 300, existingSucursal);

        ServerRequest request = mock(ServerRequest.class);

        when(request.pathVariable("productId")).thenReturn(productId.toString());
        when(request.bodyToMono(Product.class)).thenReturn(Mono.just(product));
        when(productRepository.findById(productId)).thenReturn(Optional.empty()); // No existe el producto

        Mono<ServerResponse> responseMono = productHandler.deleteProductFromSucursal(request);

        StepVerifier.create(responseMono)
                .expectNextMatches(serverResponse -> {
                    assertEquals(HttpStatus.NOT_FOUND, serverResponse.statusCode());
                    return true;
                })
                .verifyComplete();

        verify(productRepository, only()).findById(productId);

    }

    @Test
    void testUpdateStock_Success() {

        Long productId = 1L;
        Long sucursalId = 12L;
        Long franchiseId = 3L;

        Franchise franchise = new Franchise(franchiseId, "Droguerias Farmacolombia", Collections.emptyList());
        Sucursal existingSucursal = new Sucursal(sucursalId, "Sucursal del norte", franchise, Collections.emptyList());
        Product product = new Product(productId, "Acetaminofen", 300, existingSucursal);

        ServerRequest request = mock(ServerRequest.class);

        when(request.pathVariable("productId")).thenReturn(productId.toString());
        when(request.bodyToMono(Integer.class)).thenReturn(Mono.just(1));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Mono<ServerResponse> responseMono = productHandler.updateStock(request);

        StepVerifier.create(responseMono)
                .expectNextMatches(serverResponse -> {
                    assertEquals(HttpStatus.OK, serverResponse.statusCode());
                    return true;
                })
                .verifyComplete();

        verify(productRepository, times(1)).findById(productId);
        verify(productRepository, times(1)).save(product);

    }

    @Test
    void testUpdateStock_FailureWhenProductWasNotFound() {

        Long productId = 1L;

        ServerRequest request = mock(ServerRequest.class);

        when(request.pathVariable("productId")).thenReturn(productId.toString());
        when(request.bodyToMono(Integer.class)).thenReturn(Mono.just(1));
        when(productRepository.findById(productId)).thenReturn(Optional.empty()); // Producto no existe

        Mono<ServerResponse> responseMono = productHandler.updateStock(request);

        StepVerifier.create(responseMono)
                .expectNextMatches(serverResponse -> {
                    assertEquals(HttpStatus.BAD_REQUEST, serverResponse.statusCode());
                    return true;
                })
                .verifyComplete();

        verify(productRepository, only()).findById(productId);

    }

    @Test
    void testUpdateName_Success() {

        Long productId = 1L;
        Long sucursalId = 12L;
        Long franchiseId = 3L;

        Franchise franchise = new Franchise(franchiseId, "Droguerias Farmacolombia", Collections.emptyList());
        Sucursal existingSucursal = new Sucursal(sucursalId, "Sucursal del norte", franchise, Collections.emptyList());
        Product product = new Product(productId, "Acetaminofen", 300, existingSucursal);

        ServerRequest request = mock(ServerRequest.class);

        when(request.pathVariable("productId")).thenReturn(productId.toString());
        when(request.bodyToMono(Product.class)).thenReturn(Mono.just(product));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Mono<ServerResponse> responseMono = productHandler.updateProductName(request);

        StepVerifier.create(responseMono)
                .expectNextMatches(serverResponse -> {
                    assertEquals(HttpStatus.OK, serverResponse.statusCode());
                    return true;
                })
                .verifyComplete();

        verify(productRepository, times(1)).findById(productId);
        verify(productRepository, times(1)).save(product);

    }

    @Test
    void testUpdateName_FailureWhenProductWasNotFound() {

        Long productId = 1L;
        Long sucursalId = 12L;
        Long franchiseId = 3L;

        Franchise franchise = new Franchise(franchiseId, "Droguerias Farmacolombia", Collections.emptyList());
        Sucursal existingSucursal = new Sucursal(sucursalId, "Sucursal del norte", franchise, Collections.emptyList());
        Product product = new Product(productId, "Acetaminofen", 300, existingSucursal);

        ServerRequest request = mock(ServerRequest.class);

        when(request.pathVariable("productId")).thenReturn(productId.toString());
        when(request.bodyToMono(Product.class)).thenReturn(Mono.just(product));
        when(productRepository.findById(productId)).thenReturn(Optional.empty()); // Producto no encontrado

        Mono<ServerResponse> responseMono = productHandler.updateProductName(request);

        StepVerifier.create(responseMono)
                .expectNextMatches(serverResponse -> {
                    assertEquals(HttpStatus.BAD_REQUEST, serverResponse.statusCode());
                    return true;
                })
                .verifyComplete();

        verify(productRepository, times(1)).findById(productId);

    }

    @Test
    void testGetMaxStockProductBySucursal_Success() {

        Long sucursalId = 12L;
        Long franchiseId = 3L;

        Franchise franchise = new Franchise(franchiseId, "Droguerias Farmacolombia", Collections.emptyList());
        Sucursal existingSucursal = new Sucursal(sucursalId, "Sucursal del norte", franchise, Collections.emptyList());

        ServerRequest request = mock(ServerRequest.class);

        when(request.pathVariable("franchiseId")).thenReturn(franchiseId.toString());
        when(franchiseRepository.findById(franchiseId)).thenReturn(Optional.of(franchise));
        when(sucursalRepository.findByFranchiseIdWithProducts(franchiseId)).thenReturn(List.of(existingSucursal));

        Mono<ServerResponse> responseMono = productHandler.getMaxStockProductBySucursal(request);

        StepVerifier.create(responseMono)
                .expectNextMatches(serverResponse -> {
                    assertEquals(HttpStatus.OK, serverResponse.statusCode());
                    return true;
                })
                .verifyComplete();

        verify(franchiseRepository, only()).findById(franchiseId);
        verify(sucursalRepository, only()).findByFranchiseIdWithProducts(franchiseId);

    }

    @Test
    void testGetMaxStockProductBySucursal_FailureWhenFranchiseWasNotFound() {

        Long franchiseId = 3L;

        ServerRequest request = mock(ServerRequest.class);

        when(request.pathVariable("franchiseId")).thenReturn(franchiseId.toString());
        when(franchiseRepository.findById(franchiseId)).thenReturn(Optional.empty()); // Franquicia no existe

        Mono<ServerResponse> responseMono = productHandler.getMaxStockProductBySucursal(request);

        StepVerifier.create(responseMono)
                .expectNextMatches(serverResponse -> {
                    assertEquals(HttpStatus.BAD_REQUEST, serverResponse.statusCode());
                    return true;
                })
                .verifyComplete();

        verify(franchiseRepository, only()).findById(franchiseId);
        verify(sucursalRepository, never()).findByFranchiseIdWithProducts(franchiseId);

    }
}
