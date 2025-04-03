package co.com.nequi.api.handler;

import co.com.nequi.api.entity.Franchise;
import co.com.nequi.api.entity.Sucursal;
import co.com.nequi.api.repository.FranchiseRepository;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.only;

@SpringBootTest
public class SucursalHandlerTest {

    @Mock
    private FranchiseRepository franchiseRepository;

    @Mock
    private SucursalRepository sucursalRepository;


    @InjectMocks
    private SucursalHandler sucursalHandler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(true);
    }

    @Test
    void testCreateSucursal_Success() {

        Long sucursalId = 1L;
        Long franchiseId = 10L;

        Franchise existingFranchise = new Franchise(franchiseId, "New name", Collections.emptyList());
        Sucursal sucursal = new Sucursal(sucursalId, "Subway calle 45", existingFranchise, Collections.emptyList());

        ServerRequest request = mock(ServerRequest.class);

        when(request.bodyToMono(Sucursal.class)).thenReturn(Mono.just(sucursal));
        when(franchiseRepository.findById(franchiseId)).thenReturn(Optional.of(existingFranchise));
        when(sucursalRepository.save(any(Sucursal.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Mono<ServerResponse> responseMono = sucursalHandler.createSucursal(request);

        StepVerifier.create(responseMono)
                .expectNextMatches(serverResponse -> {
                    assertEquals(HttpStatus.CREATED, serverResponse.statusCode());
                    return true;
                })
                .verifyComplete();

        verify(franchiseRepository, only()).findById(franchiseId);
        verify(sucursalRepository, only()).save(sucursal);

    }

    @Test
    void testCreateSucursal_FailureWhenFranchiseWasNotFound()
    {
        Long sucursalId = 1L;
        Long franchiseId = 10L;

        Franchise existingFranchise = new Franchise(franchiseId, "New name", Collections.emptyList());
        Sucursal sucursal = new Sucursal(sucursalId, "Subway calle 45", existingFranchise, Collections.emptyList());

        ServerRequest request = mock(ServerRequest.class);
        when(request.bodyToMono(Sucursal.class)).thenReturn(Mono.just(sucursal));
        when(franchiseRepository.findById(franchiseId)).thenReturn(Optional.empty()); // No encontró la franchise

        Mono<ServerResponse> responseMono = sucursalHandler.createSucursal(request);

        StepVerifier.create(responseMono)
                .expectNextMatches(serverResponse -> {
                    assertEquals(HttpStatus.BAD_REQUEST, serverResponse.statusCode());
                    return true;
                })
                .verifyComplete();

        verify(franchiseRepository, only()).findById(franchiseId);
        verify(sucursalRepository, never()).save(sucursal);

    }

    @Test
    void testUpdateSucursalName_Success() {

        Long sucursalId = 1L;
        Long franchiseId = 10L;

        Franchise existingFranchise = new Franchise(franchiseId, "New name", Collections.emptyList());
        Sucursal sucursal = new Sucursal(sucursalId, "Subway calle 45", existingFranchise, Collections.emptyList());

        ServerRequest request = mock(ServerRequest.class);

        when(request.pathVariable("sucursalId")).thenReturn(sucursalId.toString());
        when(request.bodyToMono(Sucursal.class)).thenReturn(Mono.just(sucursal));
        when(sucursalRepository.findById(sucursalId)).thenReturn(Optional.of(sucursal));
        when(sucursalRepository.save(any(Sucursal.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Mono<ServerResponse> responseMono = sucursalHandler.updateSucursalName(request);

        StepVerifier.create(responseMono)
                .expectNextMatches(serverResponse -> {
                    assertEquals(HttpStatus.OK, serverResponse.statusCode());
                    return true;
                })
                .verifyComplete();

        verify(sucursalRepository, times(1)).findById(sucursalId);
        verify(sucursalRepository, times(1)).save(sucursal);

    }

    @Test
    void testUpdateSucursalName_FailureWhenSucursalWasNotFound() {

        Long sucursalId = 1L;
        Long franchiseId = 10L;

        Franchise existingFranchise = new Franchise(franchiseId, "New name", Collections.emptyList());
        Sucursal sucursal = new Sucursal(sucursalId, "Subway calle 45", existingFranchise, Collections.emptyList());

        ServerRequest request = mock(ServerRequest.class);

        when(request.pathVariable("sucursalId")).thenReturn(sucursalId.toString());
        when(request.bodyToMono(Sucursal.class)).thenReturn(Mono.just(sucursal));
        when(sucursalRepository.findById(sucursalId)).thenReturn(Optional.empty()); // Sucursal no encontrada

        Mono<ServerResponse> responseMono = sucursalHandler.updateSucursalName(request);

        StepVerifier.create(responseMono)
                .expectNextMatches(serverResponse -> {
                    assertEquals(HttpStatus.BAD_REQUEST, serverResponse.statusCode());
                    return true;
                })
                .verifyComplete();

        verify(sucursalRepository, times(1)).findById(sucursalId);
        verify(sucursalRepository, never()).save(sucursal);

    }
}
