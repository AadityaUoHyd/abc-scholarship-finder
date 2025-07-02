package org.aadi.abc_scholarship_finder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AbcScholarshipFinderApplication {

	static {
		Dotenv.configure()
				.ignoreIfMissing()
				.load()
				.entries()
				.forEach(e -> System.setProperty(e.getKey(), e.getValue()));
	}

	public static void main(String[] args) {
		SpringApplication.run(AbcScholarshipFinderApplication.class, args);
	}

}
