package org.aadi.abc_scholarship_finder.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
public class GoogleAiService {

    private static final Logger logger = LoggerFactory.getLogger(GoogleAiService.class);

    private final WebClient webClient;

    public GoogleAiService(@Value("${google.gemini.api-key}") String apiKey) {
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
                    .bodyToMono(Map.class)
                    .map(response -> {
                        var content = ((Map<?, ?>) ((Map<?, ?>) ((java.util.List<?>) response.get("candidates")).get(0)).get("content"));
                        var parts = (java.util.List<?>) content.get("parts");
                        return (String) ((Map<?, ?>) parts.get(0)).get("text");
                    });

        } catch (Exception e) {
            logger.error("Error generating scholarship recommendations: {}", e.getMessage(), e);
            return Mono.just(generateFallbackResponse());
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
