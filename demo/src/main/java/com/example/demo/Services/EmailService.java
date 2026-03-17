package com.example.demo.Services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;

import java.nio.charset.StandardCharsets;

@Service
public class EmailService {

    @Value("${resend.api.key}")
    private String resendApiKey;

    // Resend requires verification to send from raw domains. Usually during testing, you send from onboarding@resend.dev
    // If you verified colorbycrime.com on Resend, you can change this.
    private String fromEmail = "onboarding@resend.dev";

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    public void sendWelcomeEmail(String email, String username) throws Exception {
        Resend resend = new Resend(resendApiKey);

        try {
            String htmlContent = loadTemplate("templates/welcome-email.html");
            htmlContent = htmlContent.replace("{{name}}", username);

            CreateEmailOptions params = CreateEmailOptions.builder()
                .from(fromEmail)
                .to(email)
                .subject("Welcome to GrantWritingWithAI \uD83C\uDF89")
                .html(htmlContent)
                .build();

            CreateEmailResponse data = resend.emails().send(params);
            logger.info("Welcome email sent successfully via Resend API. ID: " + data.getId());
        } catch (ResendException e) {
            logger.error("Failed to send welcome email to " + email, e);
            e.printStackTrace();
            throw new Exception("Mail server problem");
        }
    }

    public void sendPasswordResetEmail(String email, String resetLink) throws Exception {
        Resend resend = new Resend(resendApiKey);

        try {
            String htmlContent = loadTemplate("templates/reset-password.html");
            htmlContent = htmlContent.replace("{{resetLink}}", resetLink);

            CreateEmailOptions params = CreateEmailOptions.builder()
                .from(fromEmail)
                .to(email)
                .subject("Password Reset Request - GrantWritingWithAI")
                .html(htmlContent)
                .build();

            CreateEmailResponse data = resend.emails().send(params);
            logger.info("Password reset email sent successfully via Resend API. ID: " + data.getId());
        } catch (ResendException e) {
            logger.error("Failed to send password reset email to " + email, e);
            e.printStackTrace();
            throw new Exception("Mail server problem");
        }
    }

    private String loadTemplate(String path) throws Exception {
        ClassPathResource resource = new ClassPathResource(path);
        return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    }
}
