package ru.delivery.courierdelivery.service;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.delivery.courierdelivery.model.Shipment;
import ru.delivery.courierdelivery.model.ShipmentAssignment;
import ru.delivery.courierdelivery.model.Stop;
import ru.delivery.courierdelivery.repository.ShipmentAssignmentRepository;
import ru.delivery.courierdelivery.repository.ShipmentRepository;
import ru.delivery.courierdelivery.repository.StopRepository;

@Service
@Transactional
public class ShipmentAssignmentService {

    private final ShipmentAssignmentRepository assignmentRepository;
    private final ShipmentRepository shipmentRepository;
    private final StopRepository stopRepository;
    private final RouteService routeService;

    public ShipmentAssignmentService(
            ShipmentAssignmentRepository assignmentRepository,
            ShipmentRepository shipmentRepository,
            StopRepository stopRepository,
            RouteService routeService
    ) {
        this.assignmentRepository = assignmentRepository;
        this.shipmentRepository = shipmentRepository;
        this.stopRepository = stopRepository;
        this.routeService = routeService;
    }

    @Transactional(readOnly = true)
    public List<ShipmentAssignment> findAll() {
        return assignmentRepository.findAllByOrderByIdAsc();
    }

    @Transactional(readOnly = true)
    public ShipmentAssignment findById(Long id) {
        return assignmentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Назначение не найдено"));
    }

    public ShipmentAssignment create(Long shipmentId, Long stopId, String status) {
        Shipment shipment = requireShipment(shipmentId);
        Stop stop = requireStop(stopId);
        ensureShipmentFree(shipmentId, null);
        routeService.assertFits(stop.getRoute().getId(), shipment.getWeight(), null);
        ShipmentAssignment assignment = new ShipmentAssignment();
        assignment.setShipment(shipment);
        assignment.setStop(stop);
        assignment.setStatus(status.trim());
        return assignmentRepository.save(assignment);
    }

    public ShipmentAssignment update(Long id, Long shipmentId, Long stopId, String status) {
        ShipmentAssignment assignment = findById(id);
        Shipment shipment = requireShipment(shipmentId);
        Stop stop = requireStop(stopId);
        ensureShipmentFree(shipmentId, id);
        routeService.assertFits(stop.getRoute().getId(), shipment.getWeight(), id);
        assignment.setShipment(shipment);
        assignment.setStop(stop);
        assignment.setStatus(status.trim());
        return assignmentRepository.save(assignment);
    }

    public void delete(Long id) {
        if (!assignmentRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Назначение не найдено");
        }
        assignmentRepository.deleteById(id);
    }

    private Shipment requireShipment(Long shipmentId) {
        return shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Отправление не найдено"));
    }

    private Stop requireStop(Long stopId) {
        return stopRepository.findById(stopId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Остановка не найдена"));
    }

    private void ensureShipmentFree(Long shipmentId, Long currentId) {
        boolean taken = assignmentRepository.findByShipment_Id(shipmentId).stream()
                .anyMatch(existing -> !existing.getId().equals(currentId));
        if (taken) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Отправление уже назначено на остановку");
        }
    }
}
