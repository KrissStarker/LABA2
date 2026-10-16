package ru.delivery.courierdelivery.controller;

import jakarta.validation.Valid;
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
import ru.delivery.courierdelivery.dto.StopRequest;
import ru.delivery.courierdelivery.dto.StopResponse;
import ru.delivery.courierdelivery.model.Stop;
import ru.delivery.courierdelivery.service.StopService;

@RestController
@RequestMapping("/api/stops")
public class StopController {

    private final StopService stopService;

    public StopController(StopService stopService) {
        this.stopService = stopService;
    }

    @GetMapping
    public ResponseEntity<List<StopResponse>> findAll() {
        List<StopResponse> body = stopService.findAll().stream().map(this::toResponse).toList();
        return ResponseEntity.ok(body);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StopResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(toResponse(stopService.findById(id)));
    }

    @PostMapping
    public ResponseEntity<StopResponse> create(@Valid @RequestBody StopRequest request) {
        Stop stop = stopService.create(request.routeId(), request.address(), request.sequenceNumber());
        StopResponse response = toResponse(stop);
        return ResponseEntity.created(URI.create("/api/stops/" + response.id())).body(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<StopResponse> update(@PathVariable Long id, @Valid @RequestBody StopRequest request) {
        Stop stop = stopService.update(id, request.routeId(), request.address(), request.sequenceNumber());
        return ResponseEntity.ok(toResponse(stop));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        stopService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private StopResponse toResponse(Stop stop) {
        return new StopResponse(
                stop.getId(),
                stop.getRoute().getId(),
                stop.getAddress(),
                stop.getSequenceNumber(),
                stop.getCreatedAt()
        );
    }
}
