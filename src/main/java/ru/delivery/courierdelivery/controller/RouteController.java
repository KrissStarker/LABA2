package ru.delivery.courierdelivery.controller;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.delivery.courierdelivery.dto.RouteRequest;
import ru.delivery.courierdelivery.dto.RouteResponse;
import ru.delivery.courierdelivery.model.Route;
import ru.delivery.courierdelivery.service.RouteService;

@RestController
@RequestMapping("/api/routes")
public class RouteController {

    private final RouteService routeService;

    public RouteController(RouteService routeService) {
        this.routeService = routeService;
    }

    @GetMapping
    public ResponseEntity<List<RouteResponse>> findAll() {
        List<RouteResponse> body = routeService.findAll().stream().map(this::toResponse).toList();
        return ResponseEntity.ok(body);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RouteResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(toResponse(routeService.findById(id)));
    }

    @PostMapping
    public ResponseEntity<RouteResponse> create(@Valid @RequestBody RouteRequest request) {
        Route route = routeService.create(request.routeDate(), request.maxWeight(), request.status());
        RouteResponse response = toResponse(route);
        return ResponseEntity.created(URI.create("/api/routes/" + response.id())).body(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<RouteResponse> update(@PathVariable Long id, @Valid @RequestBody RouteRequest request) {
        Route route = routeService.update(id, request.routeDate(), request.maxWeight(), request.status());
        return ResponseEntity.ok(toResponse(route));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        routeService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private RouteResponse toResponse(Route route) {
        BigDecimal currentWeight = routeService.currentWeight(route.getId());
        return new RouteResponse(
                route.getId(),
                route.getRouteDate(),
                route.getMaxWeight(),
                currentWeight,
                route.getMaxWeight().subtract(currentWeight),
                route.getStatus(),
                route.getCreatedAt()
        );
    }
}
