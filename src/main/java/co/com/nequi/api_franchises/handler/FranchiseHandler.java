package co.com.nequi.api_franchises.handler;

import co.com.nequi.api_franchises.entity.Franchise;
import co.com.nequi.api_franchises.repository.FranchiseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
@RequiredArgsConstructor
public class FranchiseHandler {
    private final FranchiseRepository franchiseRepository;

    public Mono<ServerResponse> createFranchise(ServerRequest request) {
        return request.bodyToMono(Franchise.class)
                .flatMap(franchise -> Mono.fromCallable(() -> franchiseRepository.save(franchise))
                        .subscribeOn(Schedulers.boundedElastic()))
                .flatMap(franchise -> ServerResponse.status(HttpStatus.CREATED).bodyValue(franchise));
    }


    public Mono<ServerResponse> updateFranchiseName(ServerRequest request) {
        Long franchiseId = Long.parseLong(request.pathVariable("franchiseId"));

        return request.bodyToMono(Franchise.class)
                .flatMap(newFranchiseData -> Mono.fromCallable(() -> franchiseRepository.findById(franchiseId))
                        .subscribeOn(Schedulers.boundedElastic())
                        .flatMap(optionalFranchise -> optionalFranchise.map(franchise -> {
                            franchise.setNombre(newFranchiseData.getNombre());
                            return Mono.fromCallable(() -> franchiseRepository.save(franchise))
                                    .subscribeOn(Schedulers.boundedElastic());
                        }).orElseGet(() -> Mono.error(new RuntimeException("Franchise not found")))))
                .flatMap(updatedFranchise -> ServerResponse.ok().bodyValue(updatedFranchise))
                .onErrorResume(e -> ServerResponse.status(HttpStatus.BAD_REQUEST).bodyValue(e.getMessage()));
    }
}
