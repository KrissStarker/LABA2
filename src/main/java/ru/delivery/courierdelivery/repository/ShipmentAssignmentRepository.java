package ru.delivery.courierdelivery.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.delivery.courierdelivery.model.ShipmentAssignment;

public interface ShipmentAssignmentRepository extends JpaRepository<ShipmentAssignment, Long> {

    @EntityGraph(attributePaths = {"shipment", "stop"})
    List<ShipmentAssignment> findAllByOrderByIdAsc();

    @Override
    @EntityGraph(attributePaths = {"shipment", "stop"})
    Optional<ShipmentAssignment> findById(Long id);

    @EntityGraph(attributePaths = {"shipment", "stop", "stop.route"})
    List<ShipmentAssignment> findByStop_Route_Id(Long routeId);

    @EntityGraph(attributePaths = {"shipment", "stop", "stop.route"})
    List<ShipmentAssignment> findByShipment_Id(Long shipmentId);

    @EntityGraph(attributePaths = "shipment")
    List<ShipmentAssignment> findByStop_Id(Long stopId);

    List<ShipmentAssignment> findByShipment_IdIn(Collection<Long> ids);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from ShipmentAssignment a where a.shipment.id in :ids")
    void deleteByShipmentIds(@Param("ids") Collection<Long> ids);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from ShipmentAssignment a where a.stop.id in :ids")
    void deleteByStopIds(@Param("ids") Collection<Long> ids);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from ShipmentAssignment a where a.shipment.id = :shipmentId")
    void deleteByShipmentId(@Param("shipmentId") Long shipmentId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from ShipmentAssignment a where a.stop.id = :stopId")
    void deleteByStopId(@Param("stopId") Long stopId);
}
