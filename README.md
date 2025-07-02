# abc-scholarship-finder
Spring AI project for finding scholarship across globe for students.

## The code structure
```
abc-scholarship-finder
    ├── .env
    ├── pom.xml
    ├── .gitignore
    ├── README.md
    ├── mvnw.cmd
    └── src/main/java/org/aadi/abc_scholarship_finder
            ├── AbcScholarshipFinderApplication.java
            ├── config
            │   ├── JwtAuthFilter.java
            │   ├── SecurityConfig.java
            │   └── WebConfig.java
            ├── controller
            │   ├── AuthController.java
            │   ├── ScholarshipController.java
            │   └── HomeController.java
            ├── dto
            │   ├── LoginRequest.java
            │   ├── RegisterRequest.java
            │   └── ScholarshipSearchRequest.java
            ├── exception
            │   └── GlobalExceptionHandler.java
            ├── model
            │   ├── Role.java
            │   ├── User.java
            │   └── Scholarship.java
            ├── repository
            │   ├── RoleRepository.java
            │   ├── UserRepository.java
            │   └── ScholarshipRepository.java
            └── service
                ├── AuthService.java
                ├── JwtService.java
                ├── ScholarshipService.java
                ├── UserDetailsServiceImpl.java
                └── GoogleAiService.java
            src/main/resources
            ├── application.properties
            ├── static
            │   ├── css
            │   │   └── style.css
            │   ├── images
            │   │   └── ......
            │   └── js
            │       └── script.js
            └── templates
                ├── index.html
                ├── login.html
                ├── register.html
                ├── search.html
                ├── scholarships.html
                ├── scholarship-detail.html
                └── fragments
                    └── footer.html
                    └── nav.html
            src/test/java/org/aadi/abc_scholarship_finder
            └── AbcScholarshipFinderApplicationTests.java
```

## The Tech Stack
- Spring Boot 
- PostgreSQL
- Thymeleaf
- Google Studio AI