package com.shalom.repository;

import com.shalom.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    List<Inventory> findByStockLessThanEqualOrderByStockAsc(int threshold);

    @Query("SELECT i FROM Inventory i WHERE i.stock <= i.minStock ORDER BY i.stock ASC")
    List<Inventory> findLowStock();
}
