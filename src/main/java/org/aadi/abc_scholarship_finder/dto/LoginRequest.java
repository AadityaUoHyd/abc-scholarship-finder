package org.aadi.abc_scholarship_finder.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String username;
    private String password;
}