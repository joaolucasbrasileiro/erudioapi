package br.com.erudio.data.dto.security;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;

public class ForgotPasswordDTO implements Serializable {

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email inválido")
    private String email;

    public ForgotPasswordDTO() {}

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}