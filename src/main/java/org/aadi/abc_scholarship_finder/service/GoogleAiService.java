package org.aadi.abc_scholarship_finder.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.aadi.abc_scholarship_finder.model.Scholarship;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class GoogleAiService {

    private static final Logger logger = LoggerFactory.getLogger(GoogleAiService.class);
    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GoogleAiService(@Value("${google.gemini.api-key}") String apiKey) {
        if (apiKey == null || apiKey.trim().isEmpty() || apiKey.equals("your_valid_google_api_key_here")) {
            logger.error("Google Gemini API key is missing or invalid. Please configure a valid API key in application.properties.");
            throw new IllegalStateException("Invalid Google Gemini API key");
        }
        this.webClient = WebClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com/v1beta")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
    }

    public Mono<String> generateScholarshipRecommendations(String userBackground, String userGoals, String userInterests, String existingScholarships) {
        try {
            String prompt = buildPrompt(userBackground, userGoals, userInterests, existingScholarships);

            Map<String, Object> requestBody = Map.of(
                    "contents", new Object[]{
                            Map.of("parts", new Object[]{
                                    Map.of("text", prompt)
                            })
                    }
            );

            return webClient.post()
                    .uri("/models/gemini-pro:generateContent")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), response ->
                            response.bodyToMono(String.class)
                                    .flatMap(errorBody -> {
                                        logger.error("API error: {} - {}", response.statusCode(), errorBody);
                                        return Mono.error(new RuntimeException("API error: " + errorBody));
                                    }))
                    .bodyToMono(Map.class)
                    .map(response -> {
                        try {
                            var content = ((Map<?, ?>) ((Map<?, ?>) ((List<?>) response.get("candidates")).get(0)).get("content"));
                            var parts = (List<?>) content.get("parts");
                            return (String) ((Map<?, ?>) parts.get(0)).get("text");
                        } catch (Exception e) {
                            logger.error("Failed to parse API response: {}", e.getMessage(), e);
                            return generateFallbackResponse();
                        }
                    })
                    .onErrorResume(e -> {
                        logger.error("Error generating scholarship recommendations: {}", e.getMessage(), e);
                        return Mono.just(generateFallbackResponse());
                    });

        } catch (Exception e) {
            logger.error("Error generating scholarship recommendations: {}", e.getMessage(), e);
            return Mono.just(generateFallbackResponse());
        }
    }

    public Mono<List<?>> searchGlobalScholarships(String userBackground, String userGoals, String userInterests) {
        try {
            String prompt = buildGlobalSearchPrompt(userBackground, userGoals, userInterests);

            Map<String, Object> requestBody = Map.of(
                    "contents", new Object[]{
                            Map.of("parts", new Object[]{
                                    Map.of("text", prompt)
                            })
                    }
            );

            return webClient.post()
                    .uri("/models/gemini-pro:generateContent")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), response ->
                            response.bodyToMono(String.class)
                                    .flatMap(errorBody -> {
                                        logger.error("API error: {} - {}", response.statusCode(), errorBody);
                                        return Mono.error(new RuntimeException("API error: " + errorBody));
                                    }))
                    .bodyToMono(Map.class)
                    .map(response -> {
                        try {
                            var content = ((Map<?, ?>) ((Map<?, ?>) ((List<?>) response.get("candidates")).get(0)).get("content"));
                            var parts = (List<?>) content.get("parts");
                            String text = (String) ((Map<?, ?>) parts.get(0)).get("text");
                            logger.info("Raw API response: {}", text); // Log raw response for debugging
                            return parseScholarshipsFromResponse(text);
                        } catch (Exception e) {
                            logger.error("Failed to parse API response: {}", e.getMessage(), e);
                            return List.of();
                        }
                    })
                    .onErrorResume(e -> {
                        logger.error("Error searching global scholarships: {}", e.getMessage(), e);
                        return Mono.just(List.of());
                    });

        } catch (Exception e) {
            logger.error("Error searching global scholarships: {}", e.getMessage(), e);
            return Mono.just(List.of());
        }
    }

    private List<Scholarship> parseScholarshipsFromResponse(String responseText) {
        List<Scholarship> scholarships = new ArrayList<>();
        // More flexible regex to handle variations in response format
        String scholarshipPattern = "(?s)" +
                "\\*\\*Scholarship Name:\\*\\*\\s*(.+?)(?:\\n|$)" +
                "(?:\\*\\*Provider:\\*\\*\\s*(.+?)(?:\\n|$))?" +
                "(?:\\*\\*Amount:\\*\\*\\s*(.+?)(?:\\n|$))?" +
                "(?:\\*\\*Deadline:\\*\\*\\s*(.+?)(?:\\n|$))?" +
                "(?:\\*\\*Education Level:\\*\\*\\s*(.+?)(?:\\n|$))?" +
                "(?:\\*\\*Field:\\*\\*\\s*(.+?)(?:\\n|$))?" +
                "(?:\\*\\*Country:\\*\\*\\s*(.+?)(?:\\n|$))?" +
                "(?:\\*\\*Why it's perfect for you:\\*\\*\\s*(.+?)(?:\\n|$))?" +
                "(?:\\*\\*Application Tips:\\*\\*\\s*(.+?)(?:\\n|$))?" +
                "(?:\\*\\*Learn More:\\*\\*\\s*(.+?)(?:\\n|$))?";
        Pattern pattern = Pattern.compile(scholarshipPattern, Pattern.MULTILINE | Pattern.DOTALL);
        Matcher matcher = pattern.matcher(responseText);

        while (matcher.find()) {
            try {
                Scholarship scholarship = new Scholarship();
                scholarship.setName(matcher.group(1) != null ? matcher.group(1).trim() : "Unknown");
                scholarship.setProvider(matcher.group(2) != null ? matcher.group(2).trim() : "Unknown");
                scholarship.setAmountInr(parseAmount(matcher.group(3) != null ? matcher.group(3).trim() : "0"));
                scholarship.setDeadline(parseDeadline(matcher.group(4) != null ? matcher.group(4).trim() : "Unknown"));
                scholarship.setEducationLevel(matcher.group(5) != null ? matcher.group(5).trim() : "Not specified");
                scholarship.setFieldOfStudy(matcher.group(6) != null ? matcher.group(6).trim() : "Not specified");
                scholarship.setCountry(matcher.group(7) != null ? matcher.group(7).trim() : "Not specified");
                scholarship.setEligibilityCriteria(matcher.group(8) != null ? matcher.group(8).trim() : "Not specified");
                scholarship.setWebsiteUrl(matcher.group(10) != null ? matcher.group(10).trim() : "#");
                scholarship.setActive(true);
                scholarships.add(scholarship);
                logger.info("Parsed scholarship: {}", scholarship.getName());
            } catch (Exception e) {
                logger.warn("Failed to parse scholarship: {}", matcher.group(0), e);
            }
        }
        if (scholarships.isEmpty()) {
            logger.warn("No scholarships parsed from response: {}", responseText);
        }
        return scholarships;
    }

    private BigDecimal parseAmount(String amount) {
        try {
            String cleanAmount = amount.replaceAll("[^0-9.]", "");
            return new BigDecimal(cleanAmount);
        } catch (Exception e) {
            logger.warn("Invalid amount format: {}", amount);
            return BigDecimal.ZERO;
        }
    }

    private LocalDate parseDeadline(String deadline) {
        try {
            return LocalDate.parse(deadline, DateTimeFormatter.ofPattern("dd MMMM yyyy"));
        } catch (Exception e) {
            logger.warn("Invalid deadline format: {}", deadline);
            return LocalDate.now().plusYears(1);
        }
    }

    private String buildPrompt(String userBackground, String userGoals, String userInterests, String existingScholarships) {
        String promptText = """
                You are an expert scholarship recommendation system specializing in scholarships for Indian and international students.

                Analyze the user's profile carefully and recommend the most suitable scholarships from the provided list.
                Consider academic background, field of study, financial need, career goals, and eligibility criteria.

                User Profile:
                - Background: %s
                - Goals: %s
                - Interests: %s

                Available Scholarships Database:
                %s

                Instructions:
                1. Match scholarships based on eligibility, field of study, and user goals
                2. Prioritize scholarships with upcoming deadlines
                3. Consider the scholarship amount and user's financial background
                4. Provide clear reasoning for each recommendation

                Please provide recommendations in this exact format:

                ## 🎓 Scholarship Recommendations

                Based on your profile, here are scholarships that align perfectly with your goals:

                ### 🏆 Top Matches

                **Scholarship Name:** [Name]
                **Provider:** [Organization/Institution]
                **Amount:** ₹[Amount] INR
                **Deadline:** [Date]
                **Education Level:** [Level]
                **Field:** [Subject Area]
                **Country:** [Country]
                **Why it's perfect for you:** [2-3 sentences explaining why this scholarship matches the user's profile, referencing specific aspects of their background, goals, or interests]
                **Application Tips:** [1-2 specific actionable tips for this scholarship]
                **Learn More:** [Website URL]

                ---

                [Repeat for each recommended scholarship]

                ### 💡 Additional Opportunities
                [If applicable, mention scholarships that might be worth considering with some additional preparation or in the future]

                ### 📋 Next Steps
                1. Review application requirements for your top choices
                2. Prepare necessary documents (transcripts, essays, recommendations)
                3. Set calendar reminders for deadlines
                4. Start with scholarships having the earliest deadlines

                If no scholarships are a good match, explain specifically why and provide suggestions for improving eligibility.
                """;

        return String.format(promptText,
                sanitizeInput(userBackground),
                sanitizeInput(userGoals),
                sanitizeInput(userInterests),
                sanitizeInput(existingScholarships));
    }

    private String buildGlobalSearchPrompt(String userBackground, String userGoals, String userInterests) {
        String promptText = """
                You are an expert scholarship search system specializing in finding scholarships for Indian and international students from global sources.

                Search for and recommend scholarships available worldwide that match the user's profile. Do not rely on a provided database; instead, use your knowledge to identify relevant scholarships from reputable sources.

                User Profile:
                - Background: %s
                - Goals: %s
                - Interests: %s

                Instructions:
                1. Identify scholarships that match the user's academic background, field of study, career goals, and interests
                2. Prioritize scholarships with upcoming deadlines (within the next 12 months)
                3. Consider scholarships that are accessible to students from the user's country or open internationally
                4. Provide clear reasoning for each recommendation
                5. Ensure all recommended scholarships have verifiable websites

                Please provide recommendations in this exact format:

                ## 🎓 Global Scholarship Recommendations

                Based on your profile, here are scholarships that align with your goals:

                ### 🏆 Top Matches

                **Scholarship Name:** [Name]
                **Provider:** [Organization/Institution]
                **Amount:** [Amount, specify currency]
                **Deadline:** [Date]
                **Education Level:** [Level]
                **Field:** [Subject Area]
                **Country:** [Country or 'International']
                **Why it's perfect for you:** [2-3 sentences explaining why this scholarship matches the user's profile, referencing specific aspects of their background, goals, or interests]
                **Application Tips:** [1-2 specific actionable tips for this scholarship]
                **Learn More:** [Website URL]

                ---

                [Repeat for each recommended scholarship]

                ### 💡 Additional Opportunities
                [If applicable, mention scholarships that might be worth considering with some additional preparation or in the future]

                ### 📋 Next Steps
                1. Verify eligibility on the scholarship websites
                2. Prepare necessary documents (transcripts, essays, recommendations)
                3. Set calendar reminders for deadlines
                4. Contact scholarship providers for clarification if needed

                If no scholarships are found, explain specifically why and provide suggestions for improving eligibility.
                """;

        return String.format(promptText,
                sanitizeInput(userBackground),
                sanitizeInput(userGoals),
                sanitizeInput(userInterests));
    }

    private String sanitizeInput(String input) {
        if (input == null || input.trim().isEmpty()) return "Not specified";
        return input.trim();
    }

    private String generateFallbackResponse() {
        return """
                ## 🎓 Scholarship Recommendations

                We're currently experiencing technical difficulties with our AI recommendation system.

                **What you can do:**
                1. 📞 Contact our support team for personalized assistance
                2. 🔍 Browse all available scholarships manually in our database
                3. 🎯 Use our filter system to narrow down options by field, amount, or deadline
                4. 📧 Subscribe to our newsletter for scholarship updates

                We apologize for the inconvenience and are working to resolve this issue promptly.
                """;
    }
}
