package co.com.nequi.api.handler;

import co.com.nequi.api.entity.Franchise;
import co.com.nequi.api.repository.FranchiseRepository;
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

@SpringBootTest
public class FranchiseHandlerTest {

    @Mock
    private FranchiseRepository franchiseRepository;

    @InjectMocks
    private FranchiseHandler franchiseHandler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(true);
    }

    @Test
    void testUpdateFranchiseName_Success() {

        Long franchiseId = 1L;

        Franchise existingFranchise = new Franchise(franchiseId, "Old name", Collections.emptyList());
        Franchise updateFranchise = new Franchise(franchiseId, "New name", Collections.emptyList());

        ServerRequest request = mock(ServerRequest.class);

        when(request.pathVariable("franchiseId")).thenReturn(franchiseId.toString());
        when(request.bodyToMono(Franchise.class)).thenReturn(Mono.just(updateFranchise));
        when(franchiseRepository.findById(franchiseId)).thenReturn(Optional.of(existingFranchise));
        when(franchiseRepository.save(any(Franchise.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Mono<ServerResponse> responseMono = franchiseHandler.updateFranchiseName(request);

        StepVerifier.create(responseMono)
                .expectNextMatches(serverResponse -> {
                    assertEquals(HttpStatus.OK, serverResponse.statusCode());
                    return true;
                })
                .verifyComplete();

        // Verificamos que determinados métodos se llamen N cantidad de veces
        verify(franchiseRepository, times(1)).findById(franchiseId);
        verify(franchiseRepository, times(1)).save(any(Franchise.class));

    }

    @Test
    void testUpdateFranchiseName_FailureWhenFranchiseWasNotFound() {

        Long franchiseId = 1L;

        Franchise updateFranchise = new Franchise(franchiseId, "New name", Collections.emptyList());

        ServerRequest request = mock(ServerRequest.class);

        when(request.pathVariable("franchiseId")).thenReturn(franchiseId.toString());
        when(request.bodyToMono(Franchise.class)).thenReturn(Mono.just(updateFranchise));
        when(franchiseRepository.findById(franchiseId)).thenReturn(Optional.empty()); // Un Optional vacío para que falle
        when(franchiseRepository.save(any(Franchise.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Mono<ServerResponse> responseMono = franchiseHandler.updateFranchiseName(request);

        StepVerifier.create(responseMono)
                .expectNextMatches(serverResponse -> {
                    assertEquals(HttpStatus.BAD_REQUEST, serverResponse.statusCode());
                    return true;
                })
                .verifyComplete();

        // Verificamos que determinados métodos se llamen N cantidad de veces
        verify(franchiseRepository, times(1)).findById(franchiseId);
        verify(franchiseRepository, never()).save(any(Franchise.class));

    }

    @Test
    void testCreateNewFranchise_Success() {

        Long franchiseId = 1L;
        Franchise newFranchise = new Franchise(franchiseId, "New franchise", Collections.emptyList());

        ServerRequest request = mock(ServerRequest.class);

        when(request.bodyToMono(Franchise.class)).thenReturn(Mono.just(newFranchise));
        when(franchiseRepository.save(any(Franchise.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Mono<ServerResponse> responseMono = franchiseHandler.createFranchise(request);

        StepVerifier.create(responseMono)
                .expectNextMatches(serverResponse -> {
                    assertEquals(HttpStatus.CREATED, serverResponse.statusCode());
                    return true;
                })
                .verifyComplete();

        verify(franchiseRepository, only()).save(any(Franchise.class));

    }

}
