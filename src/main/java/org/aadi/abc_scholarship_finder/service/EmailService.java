package org.aadi.abc_scholarship_finder.service;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendDeadlineReminder(String to, String scholarshipName, String deadline) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(to);
        helper.setSubject("Scholarship Deadline Reminder");
        helper.setText(String.format(
                "<h2>Reminder: %s Deadline Approaching</h2><p>The deadline for the %s is %s. Apply now!</p>",
                scholarshipName, scholarshipName, deadline
        ), true);
        mailSender.send(message);
    }
}