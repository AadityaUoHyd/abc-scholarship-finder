package org.aadi.abc_scholarship_finder.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RegisterRequest {
    private String username;
    private String password;
    private String email;
    private String firstName;
    private String lastName;
    private String educationLevel;
    private String majorField;
    private BigDecimal gpa;
    private String country;
    private String interests;
    private String goals;
}