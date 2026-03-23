package br.com.erudio.data.dto.security;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.util.Objects;

public class AccountCredentialsDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Username é obrigatório")
    private String username;

    @Size(min = 8, message = "Senha deve ter no mínimo 8 caracteres")
    private String password;

    @NotBlank(message = "Nome completo é obrigatório")
    private String fullName;

    @Email(message = "Email inválido")
    private String email;

    private String captchaToken;

    public AccountCredentialsDTO() {}

    public AccountCredentialsDTO(String username, String fullName) {
        this.username = username;
        this.fullName = fullName;
    }

    public AccountCredentialsDTO(String username, String fullName, String email) {
        this.username = username;
        this.fullName = fullName;
        this.email = email;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCaptchaToken() { return captchaToken; }
    public void setCaptchaToken(String captchaToken) { this.captchaToken = captchaToken; }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof AccountCredentialsDTO that)) return false;
        return Objects.equals(username, that.username) &&
                Objects.equals(password, that.password) &&
                Objects.equals(fullName, that.fullName) &&
                Objects.equals(email, that.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, password, fullName, email);
    }
}