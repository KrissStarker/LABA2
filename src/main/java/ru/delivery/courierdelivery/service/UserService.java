package ru.delivery.courierdelivery.service;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.delivery.courierdelivery.model.User;
import ru.delivery.courierdelivery.repository.ShipmentAssignmentRepository;
import ru.delivery.courierdelivery.repository.ShipmentRepository;
import ru.delivery.courierdelivery.repository.UserRepository;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final ShipmentRepository shipmentRepository;
    private final ShipmentAssignmentRepository assignmentRepository;

    public UserService(
            UserRepository userRepository,
            ShipmentRepository shipmentRepository,
            ShipmentAssignmentRepository assignmentRepository
    ) {
        this.userRepository = userRepository;
        this.shipmentRepository = shipmentRepository;
        this.assignmentRepository = assignmentRepository;
    }

    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findAllByOrderByIdAsc();
    }

    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден"));
    }

    public User create(String name, String email, String role) {
        String normalizedEmail = normalizeEmail(email);
        ensureEmailFree(normalizedEmail, null);
        User user = new User();
        user.setName(name.trim());
        user.setEmail(normalizedEmail);
        user.setRole(role.trim());
        return userRepository.save(user);
    }

    public User update(Long id, String name, String email, String role) {
        User user = findById(id);
        String normalizedEmail = normalizeEmail(email);
        ensureEmailFree(normalizedEmail, id);
        user.setName(name.trim());
        user.setEmail(normalizedEmail);
        user.setRole(role.trim());
        return userRepository.save(user);
    }

    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден");
        }
        List<Long> shipmentIds = shipmentRepository.findBySender_Id(id).stream()
                .map(shipment -> shipment.getId())
                .toList();
        if (!shipmentIds.isEmpty()) {
            assignmentRepository.deleteByShipmentIds(shipmentIds);
            shipmentRepository.deleteBySenderId(id);
        }
        userRepository.deleteById(id);
    }

    private void ensureEmailFree(String email, Long currentId) {
        userRepository.findByEmailIgnoreCase(email).ifPresent(existing -> {
            if (!existing.getId().equals(currentId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Пользователь с таким email уже есть");
            }
        });
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }
}
