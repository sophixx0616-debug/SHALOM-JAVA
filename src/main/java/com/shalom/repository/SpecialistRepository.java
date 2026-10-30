package com.shalom.repository;

import com.shalom.model.Specialist;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpecialistRepository extends JpaRepository<Specialist, Long> {
}
