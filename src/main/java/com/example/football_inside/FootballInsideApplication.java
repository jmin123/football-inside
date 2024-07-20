package com.example.football_inside;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories
public class FootballInsideApplication {

	public static void main(String[] args) {
		SpringApplication.run(FootballInsideApplication.class, args);
	}

}
