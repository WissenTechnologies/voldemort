package com.example.demo.Services;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    public EmailService(JavaMailSender mailSender, SpringTemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    public void sendOtpEmail(String to, String otp) {
        Context ctx = new Context();
        ctx.setVariable("otp", otp);
        ctx.setVariable("expiry", "10 minutes");
        sendEmail(to, "Your StockApp Verification Code", "otp-email", ctx);
    }

    public void sendWelcomeEmail(String to, String name) {
        Context ctx = new Context();
        ctx.setVariable("name", name);
        sendEmail(to, "Welcome to StockApp!", "welcome-email", ctx);
    }

    public void sendPasswordResetEmail(String to, String resetLink) {
        Context ctx = new Context();
        ctx.setVariable("resetLink", resetLink);
        ctx.setVariable("expiry", "30 minutes");
        sendEmail(to, "Reset Your StockApp Password", "reset-password", ctx);
    }

    private void sendEmail(String to, String subject, String template, Context ctx) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);

            helper.setFrom("aryaaangala@gmail.com");
            
            String html = templateEngine.process(template, ctx);
            helper.setText(html, true);
            mailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to send email: " + e.getMessage(), e);
        }
    }
}