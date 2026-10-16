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
import ru.delivery.courierdelivery.dto.ShipmentAssignmentRequest;
import ru.delivery.courierdelivery.dto.ShipmentAssignmentResponse;
import ru.delivery.courierdelivery.model.ShipmentAssignment;
import ru.delivery.courierdelivery.service.ShipmentAssignmentService;

@RestController
@RequestMapping("/api/shipment-assignments")
public class ShipmentAssignmentController {

    private final ShipmentAssignmentService assignmentService;

    public ShipmentAssignmentController(ShipmentAssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    @GetMapping
    public ResponseEntity<List<ShipmentAssignmentResponse>> findAll() {
        List<ShipmentAssignmentResponse> body = assignmentService.findAll().stream().map(this::toResponse).toList();
        return ResponseEntity.ok(body);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShipmentAssignmentResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(toResponse(assignmentService.findById(id)));
    }

    @PostMapping
    public ResponseEntity<ShipmentAssignmentResponse> create(@Valid @RequestBody ShipmentAssignmentRequest request) {
        ShipmentAssignment assignment = assignmentService.create(
                request.shipmentId(),
                request.stopId(),
                request.status()
        );
        ShipmentAssignmentResponse response = toResponse(assignment);
        return ResponseEntity.created(URI.create("/api/shipment-assignments/" + response.id())).body(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ShipmentAssignmentResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ShipmentAssignmentRequest request
    ) {
        ShipmentAssignment assignment = assignmentService.update(
                id,
                request.shipmentId(),
                request.stopId(),
                request.status()
        );
        return ResponseEntity.ok(toResponse(assignment));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        assignmentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private ShipmentAssignmentResponse toResponse(ShipmentAssignment assignment) {
        return new ShipmentAssignmentResponse(
                assignment.getId(),
                assignment.getShipment().getId(),
                assignment.getStop().getId(),
                assignment.getStatus(),
                assignment.getCreatedAt()
        );
    }
}
