package org.aadi.abc_scholarship_finder.exception;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public String handleRuntimeException(RuntimeException ex, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        // For simplicity, redirect to home or a generic error page
        return "redirect:/"; // Or return "error" if you have an error.html page
    }

    // You can add more specific exception handlers here
    // @ExceptionHandler(UserNotFoundException.class)
    // public String handleUserNotFound(UserNotFoundException ex, Model model) {
    //     model.addAttribute("errorMessage", ex.getMessage());
    //     return "error";
    // }
}