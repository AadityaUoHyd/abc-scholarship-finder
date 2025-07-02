# abc-scholarship-finder
The ABC-Scholarship-Finder is a web-based application built using Spring Boot, 
designed to help students discover and apply for scholarships tailored to their 
academic profiles. The project aims to streamline the scholarship search process 
by providing a user-friendly platform where students can register, log in, search 
for scholarships based on criteria like country, field of study, and education level, 
and access detailed scholarship information. The application leverages modern 
web technologies and a robust security framework to ensure a seamless and secure user experience.

## Sample Users
testuser/Password#123,
aadi/123

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