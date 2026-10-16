package ru.delivery.courierdelivery.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.delivery.courierdelivery.model.Shipment;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {

    @EntityGraph(attributePaths = "sender")
    List<Shipment> findAllByOrderByIdAsc();

    @Override
    @EntityGraph(attributePaths = "sender")
    Optional<Shipment> findById(Long id);

    List<Shipment> findBySender_Id(Long senderId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from Shipment s where s.sender.id = :senderId")
    void deleteBySenderId(@Param("senderId") Long senderId);
}
