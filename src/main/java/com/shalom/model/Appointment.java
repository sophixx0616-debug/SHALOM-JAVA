package com.shalom.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "appointments")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "service_id", nullable = false)
    private Service service;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private LocalTime time;

    @Column(nullable = false)
    private String status = "pendiente";

    private String worker;

    private LocalDateTime loyaltyStampedAt;

    public Appointment() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Service getService() { return service; }
    public void setService(Service service) { this.service = service; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public LocalTime getTime() { return time; }
    public void setTime(LocalTime time) { this.time = time; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getWorker() { return worker; }
    public void setWorker(String worker) { this.worker = worker; }

    public LocalDateTime getLoyaltyStampedAt() { return loyaltyStampedAt; }
    public void setLoyaltyStampedAt(LocalDateTime loyaltyStampedAt) { this.loyaltyStampedAt = loyaltyStampedAt; }
}
