package ru.delivery.courierdelivery.service;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.delivery.courierdelivery.model.Shipment;
import ru.delivery.courierdelivery.model.ShipmentAssignment;
import ru.delivery.courierdelivery.model.User;
import ru.delivery.courierdelivery.repository.ShipmentAssignmentRepository;
import ru.delivery.courierdelivery.repository.ShipmentRepository;
import ru.delivery.courierdelivery.repository.UserRepository;

@Service
@Transactional
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final UserRepository userRepository;
    private final ShipmentAssignmentRepository assignmentRepository;
    private final RouteService routeService;

    public ShipmentService(
            ShipmentRepository shipmentRepository,
            UserRepository userRepository,
            ShipmentAssignmentRepository assignmentRepository,
            RouteService routeService
    ) {
        this.shipmentRepository = shipmentRepository;
        this.userRepository = userRepository;
        this.assignmentRepository = assignmentRepository;
        this.routeService = routeService;
    }

    @Transactional(readOnly = true)
    public List<Shipment> findAll() {
        return shipmentRepository.findAllByOrderByIdAsc();
    }

    @Transactional(readOnly = true)
    public Shipment findById(Long id) {
        return shipmentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Отправление не найдено"));
    }

    public Shipment create(Long senderId, String address, BigDecimal weight, String status) {
        Shipment shipment = new Shipment();
        shipment.setSender(requireSender(senderId));
        shipment.setAddress(address.trim());
        shipment.setWeight(weight);
        shipment.setStatus(status.trim());
        return shipmentRepository.save(shipment);
    }

    public Shipment update(Long id, Long senderId, String address, BigDecimal weight, String status) {
        Shipment shipment = findById(id);
        List<ShipmentAssignment> links = assignmentRepository.findByShipment_Id(id);
        for (ShipmentAssignment link : links) {
            routeService.assertFits(link.getStop().getRoute().getId(), weight, link.getId());
        }
        shipment.setSender(requireSender(senderId));
        shipment.setAddress(address.trim());
        shipment.setWeight(weight);
        shipment.setStatus(status.trim());
        return shipmentRepository.save(shipment);
    }

    public void delete(Long id) {
        if (!shipmentRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Отправление не найдено");
        }
        assignmentRepository.deleteByShipmentId(id);
        shipmentRepository.deleteById(id);
    }

    private User requireSender(Long senderId) {
        return userRepository.findById(senderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Отправитель не найден"));
    }
}
