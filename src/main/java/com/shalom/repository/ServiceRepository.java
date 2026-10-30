package com.shalom.repository;

import com.shalom.model.Service;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ServiceRepository extends JpaRepository<Service, Long> {

    boolean existsByName(String name);

    Optional<Service> findByName(String name);

    List<Service> findByStatusTrueOrderByNameAsc();

    List<Service> findByCategoryAndStatusTrueOrderByNameAsc(String category);

    List<Service> findByCategoryOrderByNameAsc(String category);

    long countByStatusTrue();
}