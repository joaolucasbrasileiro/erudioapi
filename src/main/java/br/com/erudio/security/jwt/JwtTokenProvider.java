package br.com.erudio.security.jwt;

import br.com.erudio.data.dto.security.TokenDTO;
import br.com.erudio.exception.InvalidJwtAuthenticationException;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.micrometer.common.util.StringUtils;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Base64;
import java.util.Date;
import java.util.List;

@Service
public class JwtTokenProvider {

    @Value("${security.jwt.token.secret-key}")
    private String secretKey = "secret";

    @Value("${security.jwt.token.expire-lenght:secret}")
    private long validityInMilliseconds = 3600000;

    @Autowired
    private UserDetailsService userDetailsService;


    Algorithm algorithm = null;

    @PostConstruct
    protected void init() {
        secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
        algorithm = Algorithm.HMAC256(secretKey.getBytes());
    }

    public TokenDTO createAcessToken(String username, List<String> roles) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);
        String acessToken = getAcessToken(username, roles, now, validity);
        String refreshToken = getRefreshToken(username, roles, now);
        return new TokenDTO(username, true, now, validity, refreshToken, acessToken);
    }

    public TokenDTO refreshToken(String refreshToken) {
        if (tokenContainsBearer(refreshToken)){
            refreshToken = refreshToken.substring("Bearer ".length());
        }

        JWTVerifier verifier = JWT.require(algorithm).build();
        DecodedJWT decodedJWT = verifier.verify(refreshToken);

        String username = decodedJWT.getSubject();
        List<String> roles = decodedJWT.getClaim("roles").asList(String.class);
        return createAcessToken(username, roles);
    }



    private String getRefreshToken(String username, List<String> roles, Date now) {
        String issuerUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
        Date refreshTokenValidity =  new Date(now.getTime() + (validityInMilliseconds * 3));
        return JWT.create()
                .withClaim("roles", roles)
                .withIssuedAt(now)
                .withExpiresAt(refreshTokenValidity)
                .withSubject(username)
                .sign(algorithm);
    }

    private String getAcessToken(String username, List<String> roles, Date now, Date validity) {
        String issuerUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
        return JWT.create()
                .withClaim("roles", roles)
                .withIssuedAt(now)
                .withExpiresAt(validity)
                .withSubject(username)
                .withIssuer(issuerUrl)
                .sign(algorithm);
    }

    public Authentication getAuthentication(String token) {
        DecodedJWT decodedJwt = decodedToken(token);
        UserDetails userDetails = this.userDetailsService
                .loadUserByUsername(decodedJwt.getSubject());
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    private DecodedJWT decodedToken(String token) {
        Algorithm alg =  Algorithm.HMAC256(secretKey.getBytes());
        JWTVerifier verifier = JWT.require(alg).build();
        DecodedJWT decodedJWT = verifier.verify(token);
        return decodedJWT;
    }

    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        //Bearer token
        if (tokenContainsBearer(bearerToken)) {
            return bearerToken.substring("Bearer ".length());
        }
        return null;
    }

    private static boolean tokenContainsBearer(String token) {
        return StringUtils.isNotBlank(token) && token.startsWith("Bearer ");
    }

    public boolean validateToken(String token) {
        DecodedJWT decodedJwt = decodedToken(token);
        //se a data de expiração for antes de agora, retorne que já expirou, se nao retorne valido
        try {
            if (decodedJwt.getExpiresAt().before(new Date())) {
                return false;
            }
            return true;
        } catch (Exception e) {
            throw new InvalidJwtAuthenticationException("Invalid JWT token!");
        }
    }
}
