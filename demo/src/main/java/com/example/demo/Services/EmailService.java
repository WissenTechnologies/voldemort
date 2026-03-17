package com.example.demo.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    public void sendWelcomeEmail(String email, String username) throws Exception {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(email);
            helper.setSubject("Welcome to GrantWritingWithAI \uD83C\uDF89");

            String htmlContent = loadTemplate("templates/welcome-email.html");
            htmlContent = htmlContent.replace("{{name}}", username);

            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.info("Welcome email sent successfully to " + email);
        } catch (Exception e) {
            logger.error("Failed to send welcome email to " + email, e);
            e.printStackTrace();
            throw new Exception("Mail server problem");
        }
    }

    public void sendPasswordResetEmail(String email, String resetLink) throws Exception {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(email);
            helper.setSubject("Password Reset Request - GrantWritingWithAI");

            String htmlContent = loadTemplate("templates/reset-password.html");
            htmlContent = htmlContent.replace("{{resetLink}}", resetLink);

            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.info("Password reset email sent successfully to " + email);
        } catch (Exception e) {
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
