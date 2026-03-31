package com.saasproject.modules.notification.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.Map;

/**
 * Email service for sending notifications.
 * Mail sender is optional - will log warnings if not configured.
 */
@Slf4j
@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.mail.from:noreply@saasapp.com}")
    private String fromEmail;

    @Value("${app.mail.from-name:SaaS App}")
    private String fromName;

    @Value("${app.mail.enabled:false}")
    private boolean mailEnabled;

    @Autowired
    public EmailService(
            @Autowired(required = false) JavaMailSender mailSender,
            @Autowired(required = false) TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        if (mailSender == null) {
            log.warn("JavaMailSender not configured - email sending will be disabled");
        }
    }

    private boolean isMailConfigured() {
        if (!mailEnabled || mailSender == null) {
            log.warn("Email not sent - mail is not configured. Set app.mail.enabled=true and configure SMTP.");
            return false;
        }
        return true;
    }

    /**
     * Send simple text email.
     */
    @Async
    public void sendSimpleEmail(String to, String subject, String text) {
        if (!isMailConfigured())
            return;
        try {
            log.info("Sending simple email to: {}", to);
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
            log.info("Email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }

    /**
     * Send HTML email using template.
     */
    @Async
    public void sendTemplateEmail(String to, String subject, String templateName, Map<String, Object> variables) {
        if (!isMailConfigured() || templateEngine == null)
            return;
        try {
            log.info("Sending template email to: {} using template: {}", to, templateName);

            Context context = new Context();
            context.setVariables(variables);
            String htmlContent = templateEngine.process(templateName, context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail, fromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Template email sent successfully to: {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send template email to {}: {}", to, e.getMessage());
        } catch (Exception e) {
            log.error("Failed to send template email to {}: {}", to, e.getMessage());
        }
    }

    /**
     * Send welcome email to new user.
     */
    public void sendWelcomeEmail(String to, String userName, String companyName) {
        Map<String, Object> variables = Map.of(
                "userName", userName,
                "companyName", companyName,
                "loginUrl", "https://app.saasapp.com/login");
        sendTemplateEmail(to, "Welcome to " + companyName, "welcome", variables);
    }

    /**
     * Send password reset email.
     */
    public void sendPasswordResetEmail(String to, String userName, String resetToken) {
        Map<String, Object> variables = Map.of(
                "userName", userName,
                "resetUrl", "https://app.saasapp.com/reset-password?token=" + resetToken,
                "expiryHours", 24);
        sendTemplateEmail(to, "Password Reset Request", "password-reset", variables);
    }

    /**
     * Send invoice email with PDF attachment.
     */
    @Async
    public void sendInvoiceEmail(String to, String customerName, String invoiceNumber, byte[] pdfAttachment) {
        if (!isMailConfigured())
            return;
        try {
            log.info("Sending invoice email to: {}", to);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail, fromName);
            helper.setTo(to);
            helper.setSubject("Invoice " + invoiceNumber);
            helper.setText("Dear " + customerName + ",\n\nPlease find attached your invoice " + invoiceNumber
                    + ".\n\nThank you for your business!");

            if (pdfAttachment != null) {
                helper.addAttachment(invoiceNumber + ".pdf",
                        () -> new java.io.ByteArrayInputStream(pdfAttachment),
                        "application/pdf");
            }

            mailSender.send(message);
            log.info("Invoice email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send invoice email to {}: {}", to, e.getMessage());
        }
    }

    /**
     * Send low stock alert email.
     */
    public void sendLowStockAlert(String to, String productName, int currentStock, int minLevel) {
        String subject = "Low Stock Alert: " + productName;
        String text = String.format(
                "Low stock alert!\n\nProduct: %s\nCurrent Stock: %d\nMinimum Level: %d\n\nPlease reorder soon.",
                productName, currentStock, minLevel);
        sendSimpleEmail(to, subject, text);
    }

    /**
     * Send purchase order to supplier.
     */
    public void sendPurchaseOrderToSupplier(String to, String supplierName, String poNumber, byte[] pdfAttachment) {
        if (!isMailConfigured())
            return;
        try {
            log.info("Sending PO to supplier: {}", to);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail, fromName);
            helper.setTo(to);
            helper.setSubject("Purchase Order " + poNumber);
            helper.setText("Dear " + supplierName + ",\n\nPlease find attached our purchase order " + poNumber
                    + ".\n\nKindly confirm receipt and expected delivery date.\n\nBest regards");

            if (pdfAttachment != null) {
                helper.addAttachment(poNumber + ".pdf",
                        () -> new java.io.ByteArrayInputStream(pdfAttachment),
                        "application/pdf");
            }

            mailSender.send(message);
            log.info("PO email sent successfully to supplier: {}", to);
        } catch (Exception e) {
            log.error("Failed to send PO email to {}: {}", to, e.getMessage());
        }
    }
}
