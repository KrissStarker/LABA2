package ru.delivery.courierdelivery.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.delivery.courierdelivery.model.Route;
import ru.delivery.courierdelivery.model.ShipmentAssignment;
import ru.delivery.courierdelivery.repository.RouteRepository;
import ru.delivery.courierdelivery.repository.ShipmentAssignmentRepository;
import ru.delivery.courierdelivery.repository.StopRepository;

@Service
@Transactional
public class RouteService {

    private final RouteRepository routeRepository;
    private final StopRepository stopRepository;
    private final ShipmentAssignmentRepository assignmentRepository;

    public RouteService(
            RouteRepository routeRepository,
            StopRepository stopRepository,
            ShipmentAssignmentRepository assignmentRepository
    ) {
        this.routeRepository = routeRepository;
        this.stopRepository = stopRepository;
        this.assignmentRepository = assignmentRepository;
    }

    @Transactional(readOnly = true)
    public List<Route> findAll() {
        return routeRepository.findAllByOrderByIdAsc();
    }

    @Transactional(readOnly = true)
    public Route findById(Long id) {
        return routeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Маршрут не найден"));
    }

    @Transactional(readOnly = true)
    public BigDecimal currentWeight(Long routeId) {
        return assignmentRepository.findByStop_Route_Id(routeId).stream()
                .map(assignment -> assignment.getShipment().getWeight())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void assertFits(Long routeId, BigDecimal additionalWeight, Long ignoredAssignmentId) {
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Маршрут не найден"));
        BigDecimal used = assignmentRepository.findByStop_Route_Id(routeId).stream()
                .filter(assignment -> ignoredAssignmentId == null || !ignoredAssignmentId.equals(assignment.getId()))
                .map(ShipmentAssignment::getShipment)
                .map(shipment -> shipment.getWeight())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal total = used.add(additionalWeight);
        if (total.compareTo(route.getMaxWeight()) > 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Масса отправлений " + total + " превышает лимит маршрута " + route.getMaxWeight()
            );
        }
    }

    public Route create(LocalDate routeDate, BigDecimal maxWeight, String status) {
        Route route = new Route();
        route.setRouteDate(routeDate);
        route.setMaxWeight(maxWeight);
        route.setStatus(status.trim());
        return routeRepository.save(route);
    }

    public Route update(Long id, LocalDate routeDate, BigDecimal maxWeight, String status) {
        Route route = findById(id);
        BigDecimal used = currentWeight(id);
        if (used.compareTo(maxWeight) > 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Новый лимит меньше уже назначенной массы " + used
            );
        }
        route.setRouteDate(routeDate);
        route.setMaxWeight(maxWeight);
        route.setStatus(status.trim());
        return routeRepository.save(route);
    }

    public void delete(Long id) {
        if (!routeRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Маршрут не найден");
        }
        List<Long> stopIds = stopRepository.findByRoute_Id(id).stream()
                .map(stop -> stop.getId())
                .toList();
        if (!stopIds.isEmpty()) {
            assignmentRepository.deleteByStopIds(stopIds);
            stopRepository.deleteByRouteId(id);
        }
        routeRepository.deleteById(id);
    }
}
