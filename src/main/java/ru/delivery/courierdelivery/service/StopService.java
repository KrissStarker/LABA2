package ru.delivery.courierdelivery.service;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.delivery.courierdelivery.model.Route;
import ru.delivery.courierdelivery.model.ShipmentAssignment;
import ru.delivery.courierdelivery.model.Stop;
import ru.delivery.courierdelivery.repository.RouteRepository;
import ru.delivery.courierdelivery.repository.ShipmentAssignmentRepository;
import ru.delivery.courierdelivery.repository.StopRepository;

@Service
@Transactional
public class StopService {

    private final StopRepository stopRepository;
    private final RouteRepository routeRepository;
    private final ShipmentAssignmentRepository assignmentRepository;
    private final RouteService routeService;

    public StopService(
            StopRepository stopRepository,
            RouteRepository routeRepository,
            ShipmentAssignmentRepository assignmentRepository,
            RouteService routeService
    ) {
        this.stopRepository = stopRepository;
        this.routeRepository = routeRepository;
        this.assignmentRepository = assignmentRepository;
        this.routeService = routeService;
    }

    @Transactional(readOnly = true)
    public List<Stop> findAll() {
        return stopRepository.findAllByOrderBySequenceNumberAsc();
    }

    @Transactional(readOnly = true)
    public Stop findById(Long id) {
        return stopRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Остановка не найдена"));
    }

    public Stop create(Long routeId, String address, Integer sequenceNumber) {
        Route route = requireRoute(routeId);
        ensureSequenceFree(routeId, sequenceNumber, null);
        Stop stop = new Stop();
        stop.setRoute(route);
        stop.setAddress(address.trim());
        stop.setSequenceNumber(sequenceNumber);
        return stopRepository.save(stop);
    }

    public Stop update(Long id, Long routeId, String address, Integer sequenceNumber) {
        Stop stop = findById(id);
        Route route = requireRoute(routeId);
        ensureSequenceFree(routeId, sequenceNumber, id);
        Long previousRouteId = stop.getRoute().getId();
        if (!previousRouteId.equals(routeId)) {
            BigDecimal moving = assignmentRepository.findByStop_Id(id).stream()
                    .map(ShipmentAssignment::getShipment)
                    .map(shipment -> shipment.getWeight())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            if (moving.compareTo(BigDecimal.ZERO) > 0) {
                routeService.assertFits(routeId, moving, null);
            }
        }
        stop.setRoute(route);
        stop.setAddress(address.trim());
        stop.setSequenceNumber(sequenceNumber);
        return stopRepository.save(stop);
    }

    public void delete(Long id) {
        if (!stopRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Остановка не найдена");
        }
        assignmentRepository.deleteByStopId(id);
        stopRepository.deleteById(id);
    }

    private Route requireRoute(Long routeId) {
        return routeRepository.findById(routeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Маршрут не найден"));
    }

    private void ensureSequenceFree(Long routeId, Integer sequenceNumber, Long currentId) {
        stopRepository.findByRoute_IdAndSequenceNumber(routeId, sequenceNumber).ifPresent(existing -> {
            if (!existing.getId().equals(currentId)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "На маршруте уже есть остановка с таким порядковым номером"
                );
            }
        });
    }
}
