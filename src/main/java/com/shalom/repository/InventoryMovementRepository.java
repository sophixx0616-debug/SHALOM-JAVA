package com.shalom.repository;

import com.shalom.model.InventoryMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, Long> {

    List<InventoryMovement> findByProductIdOrderByCreatedAtDesc(Long productId);

    List<InventoryMovement> findAllByOrderByCreatedAtDesc();

    boolean existsByProductId(Long productId);
}