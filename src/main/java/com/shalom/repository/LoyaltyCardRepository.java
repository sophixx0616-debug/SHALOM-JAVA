package com.shalom.repository;

import com.shalom.model.LoyaltyCard;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface LoyaltyCardRepository extends JpaRepository<LoyaltyCard, Long> {
    Optional<LoyaltyCard> findByUserId(Long userId);
}
