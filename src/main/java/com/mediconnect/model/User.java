package com.mediconnect.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Mapped superclass or base Entity for all users in the MediConnect system.
 * We use JOINED inheritance strategy to map to specific tables.
 */
@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "role", discriminatorType = DiscriminatorType.STRING)
@NamedQueries({
        @NamedQuery(name = "User.findByEmail", query = "SELECT u FROM User u WHERE u.email = :email"),
        @NamedQuery(name = "User.findAll", query = "SELECT u FROM User u ORDER BY u.id"),
        @NamedQuery(name = "User.findByRole", query = "SELECT u FROM User u WHERE u.role = :role")
})
public abstract class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20, insertable = false, updatable = false)
    private Role role;

    @Column(name = "active")
    private Boolean active = true;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    // Abstract property getters that must be implemented by subclasses
    public abstract String getNom();

    public abstract void setNom(String nom);

    public abstract String getPrenom();

    public abstract void setPrenom(String prenom);

    public abstract String getTelephone();

    public abstract void setTelephone(String telephone);

    // ==================== Helper Methods ====================

    public String getFullName() {
        return getPrenom() + " " + getNom();
    }

    // ==================== Getters & Setters ====================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    // Usually role should be set via subclass instantiation instead of directly
    // mutating it due to Discriminator,
    // but JPA allows explicit setting if you ignore discriminator restrictions.
    // It's safer to only read it.

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
