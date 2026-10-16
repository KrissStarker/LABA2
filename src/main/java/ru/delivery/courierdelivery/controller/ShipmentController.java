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
import ru.delivery.courierdelivery.dto.ShipmentRequest;
import ru.delivery.courierdelivery.dto.ShipmentResponse;
import ru.delivery.courierdelivery.dto.UserResponse;
import ru.delivery.courierdelivery.model.Shipment;
import ru.delivery.courierdelivery.model.User;
import ru.delivery.courierdelivery.service.ShipmentService;

@RestController
@RequestMapping("/api/shipments")
public class ShipmentController {

    private final ShipmentService shipmentService;

    public ShipmentController(ShipmentService shipmentService) {
        this.shipmentService = shipmentService;
    }

    @GetMapping
    public ResponseEntity<List<ShipmentResponse>> findAll() {
        List<ShipmentResponse> body = shipmentService.findAll().stream().map(this::toResponse).toList();
        return ResponseEntity.ok(body);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShipmentResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(toResponse(shipmentService.findById(id)));
    }

    @PostMapping
    public ResponseEntity<ShipmentResponse> create(@Valid @RequestBody ShipmentRequest request) {
        Shipment shipment = shipmentService.create(
                request.senderId(),
                request.address(),
                request.weight(),
                request.status()
        );
        ShipmentResponse response = toResponse(shipment);
        return ResponseEntity.created(URI.create("/api/shipments/" + response.id())).body(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ShipmentResponse> update(@PathVariable Long id, @Valid @RequestBody ShipmentRequest request) {
        Shipment shipment = shipmentService.update(
                id,
                request.senderId(),
                request.address(),
                request.weight(),
                request.status()
        );
        return ResponseEntity.ok(toResponse(shipment));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        shipmentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private ShipmentResponse toResponse(Shipment shipment) {
        User sender = shipment.getSender();
        return new ShipmentResponse(
                shipment.getId(),
                new UserResponse(sender.getId(), sender.getName(), sender.getEmail(), sender.getRole(), sender.getCreatedAt()),
                shipment.getAddress(),
                shipment.getWeight(),
                shipment.getStatus(),
                shipment.getCreatedAt()
        );
    }
}
