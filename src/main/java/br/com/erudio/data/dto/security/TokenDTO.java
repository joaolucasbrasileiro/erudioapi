package br.com.erudio.data.dto.security;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

public class TokenDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String username;
    private Boolean authenticated;
    private Date create;
    private Date expiration;
    private String refreshToken;
    private String acessToken;

    public TokenDTO(){}

    public TokenDTO(String username, Boolean authenticated, Date create, Date expiration, String refreshToken, String acessToken) {
        this.username = username;
        this.authenticated = authenticated;
        this.create = create;
        this.expiration = expiration;
        this.refreshToken = refreshToken;
        this.acessToken = acessToken;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Boolean getAuthenticated() {
        return authenticated;
    }

    public void setAuthenticated(Boolean authenticated) {
        this.authenticated = authenticated;
    }

    public Date getCreate() {
        return create;
    }

    public void setCreate(Date create) {
        this.create = create;
    }

    public Date getExpiration() {
        return expiration;
    }

    public void setExpiration(Date expiration) {
        this.expiration = expiration;
    }

    public String getrefreshToken() {
        return refreshToken;
    }

    public void setrefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getAcessToken() {
        return acessToken;
    }

    public void setAcessToken(String acessToken) {
        this.acessToken = acessToken;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof TokenDTO tokenDTO)) return false;
        return Objects.equals(username, tokenDTO.username) && Objects.equals(authenticated, tokenDTO.authenticated) && Objects.equals(create, tokenDTO.create) && Objects.equals(expiration, tokenDTO.expiration) && Objects.equals(refreshToken, tokenDTO.refreshToken) && Objects.equals(acessToken, tokenDTO.acessToken);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, authenticated, create, expiration, refreshToken, acessToken);
    }
}
