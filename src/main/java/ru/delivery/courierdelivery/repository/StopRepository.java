package ru.delivery.courierdelivery.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.delivery.courierdelivery.model.Stop;

public interface StopRepository extends JpaRepository<Stop, Long> {

    @EntityGraph(attributePaths = "route")
    List<Stop> findAllByOrderBySequenceNumberAsc();

    @Override
    @EntityGraph(attributePaths = "route")
    Optional<Stop> findById(Long id);

    List<Stop> findByRoute_Id(Long routeId);

    Optional<Stop> findByRoute_IdAndSequenceNumber(Long routeId, Integer sequenceNumber);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from Stop s where s.route.id = :routeId")
    void deleteByRouteId(@Param("routeId") Long routeId);
}
