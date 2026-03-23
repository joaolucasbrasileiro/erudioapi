package br.com.erudio.controllers;

import br.com.erudio.controllers.docs.AuthControllerDocs;
import br.com.erudio.data.dto.security.*;
import br.com.erudio.services.AuthService;
import br.com.erudio.services.CaptchaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Authenticator Endpoint!")
@RestController
@RequestMapping("/auth")
public class AuthController implements AuthControllerDocs {

    @Autowired
    AuthService service;

    @Autowired
    CaptchaService captcha;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    // ── Endpoints existentes ───────────────────────────────────────────────

    @PostMapping("/signin")
    @Override
    public ResponseEntity<?> sigIn(@RequestBody AccountCredentialsDTO credentials) {
        if (credentialsIsInvalid(credentials)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid client request!");
        }
        var token = service.signIn(credentials);
        if (token == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid client request!");
        }
        return token;
    }

    @PutMapping("/refresh/{username}")
    @Override
    public ResponseEntity<?> refresh(
            @PathVariable("username") String username,
            @RequestHeader("Authorization") String refreshToken) {
        if (parametersAreInvalid(username, refreshToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid client request!");
        }
        var token = service.refresh(username, refreshToken);
        if (token == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid client request!");
        }
        return ResponseEntity.ok().body(token);
    }

    @PostMapping(value = "/createUser",
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_YAML_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_YAML_VALUE}
    )
    @Override
    @Valid
    public ResponseEntity<?> create(@RequestBody AccountCredentialsDTO credentials) {
        if (!captcha.verify(credentials.getCaptchaToken())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid Captcha!");
        }
        return ResponseEntity.ok(service.create(credentials));
    }

    @PutMapping(value = "/updateUser",
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_YAML_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_YAML_VALUE}
    )
    @Override
    @Valid
    public AccountCredentialsDTO update(@RequestBody AccountCredentialsDTO credentials) {
        return service.update(credentials);
    }

    @GetMapping(value = "/user/{username}",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_YAML_VALUE}
    )
    @Override
    public AccountCredentialsDTO getUser(@PathVariable("username") String username) {
        return service.getUser(username);
    }

    // ── Novos endpoints ────────────────────────────────────────────────────

    @Operation(summary = "Confirma conta via link enviado por email")
    @GetMapping("/confirm-account")
    public ResponseEntity<?> confirmAccount(
            @RequestParam("email") String email,
            @RequestParam("token") String token) {

        boolean confirmed = service.confirmAccount(email, token);

        if (confirmed) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header("Location", frontendUrl + "/?confirmed=true")
                    .build();
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Link inválido ou expirado.");
    }

    @Operation(summary = "Solicita código de recuperação de senha por email")
    @PostMapping(value = "/forgot-password",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordDTO dto) {
        service.forgotPassword(dto.getEmail());
        return ResponseEntity.ok().body("{\"message\": \"Se o email estiver cadastrado, você receberá o código em breve.\"}");
    }

    @Operation(summary = "Valida o código de 6 dígitos enviado por email")
    @PostMapping(value = "/validate-code",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> validateCode(@Valid @RequestBody ValidateCodeDTO dto) {
        boolean valid = service.validateCode(dto.getEmail(), dto.getCode());

        if (valid) {
            return ResponseEntity.ok().body("{\"message\": \"Código válido.\"}");
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("{\"message\": \"Código inválido ou expirado.\"}");
    }

    @Operation(summary = "Redefine a senha usando o código validado")
    @PostMapping(value = "/reset-password",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordDTO dto) {
        boolean success = service.resetPassword(dto.getEmail(), dto.getCode(), dto.getNewPassword());

        if (success) {
            return ResponseEntity.ok().body("{\"message\": \"Senha redefinida com sucesso.\"}");
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("{\"message\": \"Código inválido, expirado ou email não encontrado.\"}");
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    private static boolean parametersAreInvalid(String username, String refreshToken) {
        return StringUtils.isBlank(username) || StringUtils.isBlank(refreshToken);
    }

    private static boolean credentialsIsInvalid(AccountCredentialsDTO credentials) {
        return credentials == null ||
                StringUtils.isBlank(credentials.getPassword()) ||
                StringUtils.isBlank(credentials.getUsername());
    }
}