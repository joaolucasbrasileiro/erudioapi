package br.com.erudio.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "password_reset_token")
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false, length = 6)
    private String token;

    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;

    @Column(nullable = false)
    private Boolean used = false;

    public PasswordResetToken() {}

    public PasswordResetToken(String email, String token) {
        this.email = email;
        this.token = token;
        this.expiryDate = LocalDateTime.now().plusMinutes(15);
        this.used = false;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiryDate);
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getToken() { return token; }
    public LocalDateTime getExpiryDate() { return expiryDate; }
    public Boolean getUsed() { return used; }
    public void setUsed(Boolean used) { this.used = used; }
}