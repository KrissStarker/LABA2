package ru.delivery.courierdelivery.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.delivery.courierdelivery.model.User;

public interface UserRepository extends JpaRepository<User, Long> {

    List<User> findAllByOrderByIdAsc();

    Optional<User> findByEmailIgnoreCase(String email);
}
