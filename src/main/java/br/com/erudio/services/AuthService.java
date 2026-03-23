package br.com.erudio.services;

import br.com.erudio.data.dto.security.*;
import br.com.erudio.exception.RequiredObjectIsNullException;
import br.com.erudio.model.PasswordResetToken;
import br.com.erudio.model.Permission;
import br.com.erudio.model.User;
import br.com.erudio.repository.PasswordResetTokenRepository;
import br.com.erudio.repository.UserRepository;
import br.com.erudio.security.jwt.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private UserRepository repository;

    @Autowired
    private PasswordResetTokenRepository resetTokenRepository;

    @Autowired
    private EmailService emailService;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    // ── Login ──────────────────────────────────────────────────────────────

    public ResponseEntity<TokenDTO> signIn(AccountCredentialsDTO credentials) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            credentials.getUsername(),
                            credentials.getPassword()
                    )
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        var user = repository.findByUserName(credentials.getUsername());
        if (user == null) {
            throw new UsernameNotFoundException("User not found: " + credentials.getUsername());
        }

        var token = tokenProvider.createAcessToken(credentials.getUsername(), user.getRoles());
        return ResponseEntity.ok(token);
    }

    // ── Refresh token ──────────────────────────────────────────────────────

    public ResponseEntity<TokenDTO> refresh(String username, String refreshToken) {
        var user = repository.findByUserName(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }
        return ResponseEntity.ok(tokenProvider.refreshToken(refreshToken));
    }

    // ── Criar conta ────────────────────────────────────────────────────────

    public AccountCredentialsDTO create(AccountCredentialsDTO dto) {
        if (dto == null) throw new RequiredObjectIsNullException();

        logger.info("Creating new user: {}", dto.getUsername());

        var permission = new Permission();
        permission.setId(3L);

        var entity = new User();
        entity.setFullName(dto.getFullName());
        entity.setUserName(dto.getUsername());
        entity.setEmail(dto.getEmail());
        entity.setPassword(generateHashedPassword(dto.getPassword()));
        entity.setAccountNonExpired(true);
        entity.setAccountNonLocked(true);
        entity.setCredentialsNonExpired(true);
        entity.setEnabled(false); // desabilitado até confirmar o email
        entity.setPermissions(List.of(permission));

        var saved = repository.save(entity);

        // Gera token de confirmação (UUID simples, não o de 6 dígitos)
        String confirmToken = java.util.UUID.randomUUID().toString();

        // Reutiliza a tabela password_reset_token com token longo para confirmação
        // Salva com token UUID (não precisa de expiração de 6 dígitos aqui)
        var resetToken = new PasswordResetToken(saved.getEmail(), confirmToken);
        resetTokenRepository.save(resetToken);

        String confirmLink = baseUrl + "/auth/confirm-account?token=" + confirmToken
                + "&email=" + saved.getEmail();

        emailService.sendConfirmationEmail(saved.getEmail(), saved.getFullName(), confirmLink);

        return new AccountCredentialsDTO(saved.getUsername(), saved.getFullName(), saved.getEmail());
    }

    // ── Confirmar conta via link do email ──────────────────────────────────

    public boolean confirmAccount(String email, String token) {
        var resetToken = resetTokenRepository.findValidToken(email, token);

        if (resetToken.isEmpty() || resetToken.get().isExpired()) {
            return false;
        }

        var user = repository.findByEmail(email);
        if (user == null) return false;

        user.setEnabled(true);
        repository.save(user);

        resetToken.get().setUsed(true);
        resetTokenRepository.save(resetToken.get());

        return true;
    }

    // ── Esqueci minha senha ────────────────────────────────────────────────

    public void forgotPassword(String email) {
        var user = repository.findByEmail(email);

        // Não revela se o email existe ou não por segurança
        if (user == null) {
            logger.warn("Password reset requested for non-existent email: {}", email);
            return;
        }

        // Remove tokens anteriores deste email
        resetTokenRepository.deleteAllByEmail(email);

        // Gera código de 6 dígitos
        String code = generateSixDigitCode();
        var token = new PasswordResetToken(email, code);
        resetTokenRepository.save(token);

        emailService.sendPasswordResetCode(email, code);
        logger.info("Password reset code sent to {}", email);
    }

    // ── Validar código ─────────────────────────────────────────────────────

    public boolean validateCode(String email, String code) {
        var token = resetTokenRepository.findValidToken(email, code);

        if (token.isEmpty()) return false;
        if (token.get().isExpired()) return false;

        return true;
    }

    // ── Redefinir senha ────────────────────────────────────────────────────

    public boolean resetPassword(String email, String code, String newPassword) {
        var tokenOpt = resetTokenRepository.findValidToken(email, code);

        if (tokenOpt.isEmpty() || tokenOpt.get().isExpired()) {
            return false;
        }

        var user = repository.findByEmail(email);
        if (user == null) return false;

        user.setPassword(generateHashedPassword(newPassword));
        repository.save(user);

        tokenOpt.get().setUsed(true);
        resetTokenRepository.save(tokenOpt.get());

        return true;
    }

    // ── Atualizar usuário ──────────────────────────────────────────────────

    public AccountCredentialsDTO update(AccountCredentialsDTO dto) {
        if (dto == null) throw new RequiredObjectIsNullException();

        logger.info("Updating user: {}", dto.getUsername());

        var entity = repository.findByUserName(dto.getUsername());
        if (entity == null) {
            throw new UsernameNotFoundException("User not found: " + dto.getUsername());
        }

        if (dto.getFullName() != null) entity.setFullName(dto.getFullName());
        if (dto.getPassword() != null) entity.setPassword(generateHashedPassword(dto.getPassword()));
        if (dto.getEmail() != null) entity.setEmail(dto.getEmail());

        var saved = repository.save(entity);
        return new AccountCredentialsDTO(saved.getUsername(), saved.getFullName(), saved.getEmail());
    }

    // ── Buscar usuário ─────────────────────────────────────────────────────

    public AccountCredentialsDTO getUser(String username) {
        var entity = repository.findByUserName(username);
        if (entity == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }
        return new AccountCredentialsDTO(entity.getUsername(), entity.getFullName(), entity.getEmail());
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    private String generateHashedPassword(String password) {
        Map<String, PasswordEncoder> encoders = new HashMap<>();
        PasswordEncoder pbkdf2Encoder = new Pbkdf2PasswordEncoder(
                "", 8, 185000,
                Pbkdf2PasswordEncoder.SecretKeyFactoryAlgorithm.PBKDF2WithHmacSHA256
        );
        encoders.put("pbkdf2", pbkdf2Encoder);
        DelegatingPasswordEncoder passwordEncoder = new DelegatingPasswordEncoder("pbkdf2", encoders);
        passwordEncoder.setDefaultPasswordEncoderForMatches(pbkdf2Encoder);
        return passwordEncoder.encode(password);
    }

    private String generateSixDigitCode() {
        SecureRandom random = new SecureRandom();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }
}