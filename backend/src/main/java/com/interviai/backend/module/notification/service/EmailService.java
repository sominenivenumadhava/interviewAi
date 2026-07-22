package com.interviai.backend.module.notification.service;

import com.interviai.backend.module.notification.dto.EmailRequest;

import java.util.concurrent.CompletableFuture;

public interface EmailService {
    
    /**
     * Send an email using a template
     * @param request Email request with template and variables
     * @return CompletableFuture indicating completion
     */
    CompletableFuture<Void> sendTemplatedEmail(EmailRequest request);
    
    /**
     * Send a simple email without template
     * @param to Recipient email
     * @param subject Email subject
     * @param content Email content
     * @return CompletableFuture indicating completion
     */
    CompletableFuture<Void> sendSimpleEmail(String to, String subject, String content);
    
    /**
     * Send welcome email to new user
     * @param userEmail User's email
     * @param userName User's name
     * @param verificationLink Email verification link
     */
    void sendWelcomeEmail(String userEmail, String userName, String verificationLink);
    
    /**
     * Send password reset email
     * @param userEmail User's email
     * @param userName User's name
     * @param resetLink Password reset link
     */
    void sendPasswordResetEmail(String userEmail, String userName, String resetLink);
    
    /**
     * Send interview scheduled notification
     * @param userEmail User's email
     * @param interviewDetails Interview details
     */
    void sendInterviewScheduledEmail(String userEmail, String interviewDetails);
    
    /**
     * Send interview reminder
     * @param userEmail User's email
     * @param interviewDetails Interview details
     * @param hoursUntilInterview Hours until the interview
     */
    void sendInterviewReminderEmail(String userEmail, String interviewDetails, int hoursUntilInterview);
    
    /**
     * Send interview completion summary
     * @param userEmail User's email
     * @param interviewSummary Summary of the completed interview
     */
    void sendInterviewCompletionEmail(String userEmail, String interviewSummary);
    
    /**
     * Send weekly progress report
     * @param userEmail User's email
     * @param progressReport Weekly progress data
     */
    void sendWeeklyProgressEmail(String userEmail, String progressReport);
}