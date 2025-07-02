package org.aadi.abc_scholarship_finder.service;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.aadi.abc_scholarship_finder.model.Scholarship;
import org.aadi.abc_scholarship_finder.model.User;
import org.aadi.abc_scholarship_finder.repository.UserRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReminderService {

    private final UserRepository userRepository;
    private final EmailService emailService;

    @Scheduled(cron = "0 0 8 * * ?") // Daily at 8 AM
    public void sendDeadlineReminders() {
        List<User> users = userRepository.findAll();
        for (User user : users) {
            for (Scholarship scholarship : user.getFavoriteScholarships()) {
                LocalDate deadline = scholarship.getDeadline();
                if (deadline != null && deadline.minusDays(7).isBefore(LocalDate.now())) {
                    try {
                        emailService.sendDeadlineReminder(
                                user.getEmail(),
                                scholarship.getName(),
                                deadline.toString()
                        );
                    } catch (MessagingException e) {
                        // Log error
                    }
                }
            }
        }
    }
}