package org.aadi.abc_scholarship_finder.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.aadi.abc_scholarship_finder.dto.LoginRequest;
import org.aadi.abc_scholarship_finder.dto.RegisterRequest;
import org.aadi.abc_scholarship_finder.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @GetMapping("/login")
    public String showLoginForm(Model model) {
        logger.debug("Rendering login form");
        model.addAttribute("loginRequest", new LoginRequest());
        return "login";
    }

    @PostMapping("/api/auth/login")
    public String login(@ModelAttribute("loginRequest") LoginRequest loginRequest,
                        HttpServletResponse response,
                        RedirectAttributes redirectAttributes) {
        logger.info("Login attempt for username: {}", loginRequest.getUsername());
        try {
            String token = authService.login(loginRequest);
            Cookie jwtCookie = new Cookie("jwt", token);
            jwtCookie.setHttpOnly(true);
            jwtCookie.setPath("/");
            jwtCookie.setMaxAge(60 * 60 * 24);
            response.addCookie(jwtCookie);
            logger.info("Login successful for username: {}", loginRequest.getUsername());
            redirectAttributes.addFlashAttribute("successMessage", "Login successful! Welcome back.");
            return "redirect:/scholarships/search";
        } catch (Exception e) {
            logger.error("Login failed for username: {}. Error: {}", loginRequest.getUsername(), e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Login failed: Invalid username or password");
            return "redirect:/login";
        }
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        logger.debug("Rendering register form");
        model.addAttribute("registerRequest", new RegisterRequest());
        return "register";
    }

    @PostMapping("/api/auth/register")
    public String register(@ModelAttribute("registerRequest") RegisterRequest registerRequest,
                           @RequestParam(value = "confirmPassword", required = false) String confirmPassword,
                           HttpServletResponse response,
                           RedirectAttributes redirectAttributes) {
        logger.info("Registration attempt for username: {}", registerRequest.getUsername());
        if (confirmPassword == null || !registerRequest.getPassword().equals(confirmPassword)) {
            logger.warn("Password confirmation failed for username: {}", registerRequest.getUsername());
            redirectAttributes.addFlashAttribute("errorMessage", "Passwords do not match!");
            return "redirect:/register";
        }
        try {
            String token = authService.register(registerRequest);
            Cookie jwtCookie = new Cookie("jwt", token);
            jwtCookie.setHttpOnly(true);
            jwtCookie.setPath("/");
            jwtCookie.setMaxAge(60 * 60 * 24);
            response.addCookie(jwtCookie);
            logger.info("Registration successful for username: {}", registerRequest.getUsername());
            redirectAttributes.addFlashAttribute("successMessage", "Registration successful! You are now logged in.");
            return "redirect:/scholarships/search";
        } catch (RuntimeException e) {
            logger.error("Registration failed for username: {}. Error: {}", registerRequest.getUsername(), e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Registration failed: " + e.getMessage());
            return "redirect:/register";
        }
    }
}