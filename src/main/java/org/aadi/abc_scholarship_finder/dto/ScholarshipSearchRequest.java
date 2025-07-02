package org.aadi.abc_scholarship_finder.dto;

import lombok.Data;

@Data
public class ScholarshipSearchRequest {
    private String country;
    private String field;
    private String educationLevel;
}
