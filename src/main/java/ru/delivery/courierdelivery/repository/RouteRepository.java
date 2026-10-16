package ru.delivery.courierdelivery.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.delivery.courierdelivery.model.Route;

public interface RouteRepository extends JpaRepository<Route, Long> {

    List<Route> findAllByOrderByIdAsc();
}
