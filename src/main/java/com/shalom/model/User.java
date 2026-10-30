package com.shalom.model;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String lastName;

    private String phone;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private LoyaltyCard loyaltyCard;

    private String googleId;

    private String photoUrl;

    private boolean enabled = true;

    public User() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public LoyaltyCard getLoyaltyCard() { return loyaltyCard; }
    public void setLoyaltyCard(LoyaltyCard loyaltyCard) { this.loyaltyCard = loyaltyCard; }

    public String getGoogleId() { return googleId; }
    public void setGoogleId(String googleId) { this.googleId = googleId; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}
