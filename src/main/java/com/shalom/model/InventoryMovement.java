package com.shalom.model;

import com.shalom.model.enums.MovementType;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_movements")
public class InventoryMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Inventory product;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MovementType type;

    private int quantity;

    private int stockBefore;

    private int stockAfter;

    private String reason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public InventoryMovement() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Inventory getProduct() { return product; }
    public void setProduct(Inventory product) { this.product = product; }

    public MovementType getType() { return type; }
    public void setType(MovementType type) { this.type = type; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public int getStockBefore() { return stockBefore; }
    public void setStockBefore(int stockBefore) { this.stockBefore = stockBefore; }

    public int getStockAfter() { return stockAfter; }
    public void setStockAfter(int stockAfter) { this.stockAfter = stockAfter; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}