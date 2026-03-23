package br.com.erudio.model;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "users")
public class User implements UserDetails, Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_name", nullable = false, unique = true)
    private String userName;

    @Column(name = "full_name", nullable = false, unique = true)
    private String fullName;

    @Column(unique = true)
    private String email;

    @Column
    private String password;

    @Column(name = "account_non_expired")
    private Boolean accountNonExpired;

    @Column(name = "account_non_locked")
    private Boolean accountNonLocked;

    @Column(name = "credentials_non_expired")
    private Boolean credentialsNonExpired;

    @Column
    private Boolean enabled;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_permission",
            joinColumns = {@JoinColumn(name = "id_user")},
            inverseJoinColumns = {@JoinColumn(name = "id_permission")}
    )
    private List<Permission> permissions;

    public User() {}

    public List<String> getRoles() {
        List<String> roles = new ArrayList<>();
        for (Permission permission : permissions) {
            roles.add(permission.getDescription());
        }
        return roles;
    }

    public Long getId() { return id; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public List<Permission> getPermissions() { return permissions; }
    public Boolean getAccountNonExpired() { return accountNonExpired; }
    public Boolean getAccountNonLocked() { return accountNonLocked; }
    public Boolean getCredentialsNonExpired() { return credentialsNonExpired; }
    public Boolean getEnabled() { return enabled; }

    public void setUserName(String userName) { this.userName = userName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setAccountNonExpired(Boolean accountNonExpired) { this.accountNonExpired = accountNonExpired; }
    public void setAccountNonLocked(Boolean accountNonLocked) { this.accountNonLocked = accountNonLocked; }
    public void setCredentialsNonExpired(Boolean credentialsNonExpired) { this.credentialsNonExpired = credentialsNonExpired; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public void setPermissions(List<Permission> permissions) { this.permissions = permissions; }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof User user)) return false;
        return Objects.equals(id, user.id) &&
                Objects.equals(userName, user.userName) &&
                Objects.equals(fullName, user.fullName) &&
                Objects.equals(email, user.email) &&
                Objects.equals(password, user.password) &&
                Objects.equals(accountNonExpired, user.accountNonExpired) &&
                Objects.equals(accountNonLocked, user.accountNonLocked) &&
                Objects.equals(credentialsNonExpired, user.credentialsNonExpired) &&
                Objects.equals(enabled, user.enabled) &&
                Objects.equals(permissions, user.permissions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userName, fullName, email, password,
                accountNonExpired, accountNonLocked, credentialsNonExpired, enabled, permissions);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() { return this.permissions; }

    @Override
    public String getPassword() { return this.password; }

    @Override
    public String getUsername() { return this.userName; }

    @Override
    public boolean isAccountNonExpired() { return this.accountNonExpired; }

    @Override
    public boolean isAccountNonLocked() { return this.accountNonLocked; }

    @Override
    public boolean isCredentialsNonExpired() { return this.credentialsNonExpired; }

    @Override
    public boolean isEnabled() { return this.enabled; }
}