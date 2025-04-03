package co.com.nequi.api.handler;

import co.com.nequi.api.entity.Sucursal;
import co.com.nequi.api.repository.FranchiseRepository;
import co.com.nequi.api.repository.SucursalRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
public class SucursalHandler {

    private final SucursalRepository sucursalRepository;
    private final FranchiseRepository franchiseRepository;

    public SucursalHandler(SucursalRepository sucursalRepository, FranchiseRepository franchiseRepository) {
        this.sucursalRepository = sucursalRepository;
        this.franchiseRepository = franchiseRepository;
    }

    public Mono<ServerResponse> createSucursal(ServerRequest request) {
        return request.bodyToMono(Sucursal.class)
                .flatMap(sucursal -> Mono.justOrEmpty(franchiseRepository.findById(sucursal.getFranchise().getId()))
                        .switchIfEmpty(Mono.error(new RuntimeException("Franchise not found")))
                        .flatMap(franchise -> {
                            sucursal.setFranchise(franchise);
                            return Mono.fromCallable(() -> sucursalRepository.save(sucursal))
                                    .subscribeOn(Schedulers.boundedElastic());
                        }))
                .flatMap(savedSucursal -> ServerResponse.status(HttpStatus.CREATED).bodyValue(savedSucursal))
                .onErrorResume(e -> ServerResponse.status(HttpStatus.BAD_REQUEST).bodyValue(e.getMessage()))
                .switchIfEmpty(ServerResponse.badRequest().build());
    }

    public Mono<ServerResponse> updateSucursalName(ServerRequest request) {
        Long sucursalId = Long.parseLong(request.pathVariable("sucursalId"));

        return request.bodyToMono(Sucursal.class)
                .flatMap(newSucursalData -> Mono.fromCallable(() -> sucursalRepository.findById(sucursalId))
                        .subscribeOn(Schedulers.boundedElastic())
                        .flatMap(optionalSucursal -> optionalSucursal.map(sucursal -> {
                            sucursal.setNombre(newSucursalData.getNombre());
                            return Mono.fromCallable(() -> sucursalRepository.save(sucursal))
                                    .subscribeOn(Schedulers.boundedElastic());
                        }).orElseGet(() -> Mono.error(new RuntimeException("Sucursal not found")))))
                .flatMap(updatedSucursal -> ServerResponse.ok().bodyValue(updatedSucursal))
                .onErrorResume(e -> ServerResponse.status(HttpStatus.BAD_REQUEST).bodyValue(e.getMessage()));
    }

}
