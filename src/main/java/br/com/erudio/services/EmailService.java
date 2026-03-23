package br.com.erudio.services;

import br.com.erudio.config.EmailConfig;
import br.com.erudio.data.dto.request.EmailRequestDTO;
import br.com.erudio.mail.EmailSender;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Service
public class EmailService {

    @Autowired
    private EmailSender emailSender;

    @Autowired
    private EmailConfig emailConfigs;

    public void sendSimpleEmail(EmailRequestDTO emailRequest) {
        emailSender
            .to(emailRequest.getTo())
            .withSubject(emailRequest.getSubject())
            .withMessage(emailRequest.getSubject())
            .send(emailConfigs);
    }

    public void setEmailWithAttachment(String emailRequestJson, MultipartFile attachment) {
        File tempFile = null;
        try {
            EmailRequestDTO emailRequest = new ObjectMapper().readValue(emailRequestJson, EmailRequestDTO.class);
            tempFile = File.createTempFile("attachment", attachment.getOriginalFilename());
            attachment.transferTo(tempFile);

            emailSender
                .to(emailRequest.getTo())
                .withSubject(emailRequest.getSubject())
                .withMessage(emailRequest.getSubject())
                .attach(tempFile.getAbsolutePath())
                .send(emailConfigs);

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error parsing email request JSON!", e);
        } catch (IOException e) {
            throw new RuntimeException("Error processing the attachment!", e);
        } finally {
            if (tempFile != null && tempFile.exists()) tempFile.delete();
        }

    }

    public void sendConfirmationEmail(String toEmail, String fullName, String confirmationLink) {
        String html = """
        <div style="font-family: Inter, sans-serif; max-width: 480px; margin: 0 auto; background: #111118; border: 1px solid #222235; border-radius: 16px; padding: 32px;">
          <h2 style="font-family: Syne, sans-serif; color: #f0f0fa; margin-bottom: 8px;">Bem vindo, %s!</h2>
          <p style="color: #9090b0; font-size: 14px; margin-bottom: 24px;">
            Clique no botão abaixo para confirmar sua conta. O link expira em <strong style="color: #e8e8f5;">24 horas</strong>.
          </p>
          <a href="%s"
             style="display: inline-block; padding: 13px 28px; background: linear-gradient(135deg, #7c6af7, #2979ff);
                    color: #fff; text-decoration: none; border-radius: 10px; font-weight: 600; font-size: 15px;">
            Confirmar conta →
          </a>
          <p style="color: #55556a; font-size: 12px; margin-top: 24px;">
            Se você não criou esta conta, ignore este email.
          </p>
        </div>
        """.formatted(fullName, confirmationLink);

        emailSender
                .to(toEmail)
                .withSubject("Confirme sua conta — Erudio")
                .withMessage(html)
                .send(emailConfigs);
    }

    public void sendPasswordResetCode(String toEmail, String code) {
        String html = """
        <div style="font-family: Inter, sans-serif; max-width: 480px; margin: 0 auto; background: #111118; border: 1px solid #222235; border-radius: 16px; padding: 32px;">
          <h2 style="font-family: Syne, sans-serif; color: #f0f0fa; margin-bottom: 8px;">Recuperação de senha</h2>
          <p style="color: #9090b0; font-size: 14px; margin-bottom: 24px;">
            Use o código abaixo para redefinir sua senha. Ele expira em <strong style="color: #e8e8f5;">15 minutos</strong>.
          </p>
          <div style="background: #1a1a28; border: 1px solid #2e2e48; border-radius: 12px; padding: 24px; text-align: center; margin-bottom: 24px;">
            <span style="font-size: 36px; font-weight: 700; letter-spacing: 12px; color: #7c6af7;">%s</span>
          </div>
          <p style="color: #55556a; font-size: 12px;">
            Se você não solicitou a recuperação de senha, ignore este email.
          </p>
        </div>
        """.formatted(code);

        emailSender
                .to(toEmail)
                .withSubject("Código de recuperação de senha — Erudio")
                .withMessage(html)
                .send(emailConfigs);
    }
}
