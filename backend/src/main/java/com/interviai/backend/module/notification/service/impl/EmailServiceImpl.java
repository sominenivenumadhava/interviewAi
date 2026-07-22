package com.interviai.backend.module.notification.service.impl;

import com.interviai.backend.module.notification.dto.EmailRequest;
import com.interviai.backend.module.notification.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class EmailServiceImpl implements EmailService {
    
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    
    public EmailServiceImpl(
            @Autowired(required = false) JavaMailSender mailSender,
            @Autowired(required = false) TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }
    
    @Value("${spring.mail.username:noreply@interviai.com}")
    private String fromEmail;
    
    @Value("${app.mail.from-name:InterviAI}")
    private String fromName;
    
    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;
    
    @Override
    @Async
    public CompletableFuture<Void> sendTemplatedEmail(EmailRequest request) {
        if (mailSender == null) {
            log.warn("JavaMailSender is not configured. Skipping email to: {}", request.getTo());
            return CompletableFuture.completedFuture(null);
        }
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            
            helper.setFrom(fromEmail, fromName);
            helper.setTo(request.getTo());
            helper.setSubject(request.getSubject());
            
            if (request.getTemplateName() != null && templateEngine != null) {
                Context context = new Context();
                context.setVariables(request.getVariables() != null ? request.getVariables() : new HashMap<>());
                context.setVariable("frontendUrl", frontendUrl);
                
                String htmlContent = templateEngine.process(request.getTemplateName(), context);
                helper.setText(htmlContent, true);
            } else if (request.getHtmlContent() != null) {
                helper.setText(request.getHtmlContent(), true);
            } else {
                helper.setText(request.getPlainTextContent(), false);
            }
            
            mailSender.send(mimeMessage);
            log.info("Email sent successfully to: {}", request.getTo());
            
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", request.getTo(), e.getMessage());
            // Do not throw an exception here, as it may break the calling transaction (e.g., user registration)
        }
        
        return CompletableFuture.completedFuture(null);
    }
    
    @Override
    @Async
    public CompletableFuture<Void> sendSimpleEmail(String to, String subject, String content) {
        if (mailSender == null) {
            log.warn("JavaMailSender is not configured. Skipping simple email to: {}", to);
            return CompletableFuture.completedFuture(null);
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);
            
            mailSender.send(message);
            log.info("Simple email sent successfully to: {}", to);
            
        } catch (Exception e) {
            log.error("Failed to send simple email to {}: {}", to, e.getMessage());
            // Do not throw an exception here
        }
        
        return CompletableFuture.completedFuture(null);
    }
    
    @Override
    public void sendWelcomeEmail(String userEmail, String userName, String verificationLink) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("userName", userName);
        variables.put("verificationLink", verificationLink);
        variables.put("supportEmail", "support@interviai.com");
        
        EmailRequest request = EmailRequest.builder()
                .to(userEmail)
                .subject("Welcome to InterviAI - Verify Your Email")
                .templateName("welcome-email")
                .variables(variables)
                .build();
        
        sendTemplatedEmail(request);
    }
    
    @Override
    public void sendPasswordResetEmail(String userEmail, String userName, String resetLink) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("userName", userName);
        variables.put("resetLink", resetLink);
        variables.put("expirationHours", "24");
        
        EmailRequest request = EmailRequest.builder()
                .to(userEmail)
                .subject("Reset Your InterviAI Password")
                .templateName("password-reset")
                .variables(variables)
                .build();
        
        sendTemplatedEmail(request);
    }
    
    @Override
    public void sendInterviewScheduledEmail(String userEmail, String interviewDetails) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("interviewDetails", interviewDetails);
        
        EmailRequest request = EmailRequest.builder()
                .to(userEmail)
                .subject("Interview Scheduled - InterviAI")
                .templateName("interview-scheduled")
                .variables(variables)
                .build();
        
        sendTemplatedEmail(request);
    }
    
    @Override
    public void sendInterviewReminderEmail(String userEmail, String interviewDetails, int hoursUntilInterview) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("interviewDetails", interviewDetails);
        variables.put("hoursUntilInterview", hoursUntilInterview);
        
        EmailRequest request = EmailRequest.builder()
                .to(userEmail)
                .subject("Interview Reminder - Starting in " + hoursUntilInterview + " hours")
                .templateName("interview-reminder")
                .variables(variables)
                .build();
        
        sendTemplatedEmail(request);
    }
    
    @Override
    public void sendInterviewCompletionEmail(String userEmail, String interviewSummary) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("interviewSummary", interviewSummary);
        
        EmailRequest request = EmailRequest.builder()
                .to(userEmail)
                .subject("Interview Completed - View Your Results")
                .templateName("interview-completion")
                .variables(variables)
                .build();
        
        sendTemplatedEmail(request);
    }
    
    @Override
    public void sendWeeklyProgressEmail(String userEmail, String progressReport) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("progressReport", progressReport);
        
        EmailRequest request = EmailRequest.builder()
                .to(userEmail)
                .subject("Your Weekly Progress Report - InterviAI")
                .templateName("weekly-progress")
                .variables(variables)
                .build();
        
        sendTemplatedEmail(request);
    }
}