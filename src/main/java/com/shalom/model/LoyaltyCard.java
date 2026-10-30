package com.shalom.model;

import jakarta.persistence.*;

@Entity
@Table(name = "loyalty_cards")
public class LoyaltyCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

    private int stamps = 0;

    private int availableRewards = 0;

    public LoyaltyCard() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public int getStamps() { return stamps; }
    public void setStamps(int stamps) { this.stamps = stamps; }

    public int getAvailableRewards() { return availableRewards; }
    public void setAvailableRewards(int availableRewards) { this.availableRewards = availableRewards; }

    public void addStamp() {
        this.stamps++;
        if (this.stamps >= 9) {
            this.stamps = 0;
            this.availableRewards++;
        }
    }
}
