package com.example.football_inside;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Base64;

@SpringBootTest
class FootballInsideApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	void test() {
		byte[] keyBytes = Keys.secretKeyFor(SignatureAlgorithm.HS512).getEncoded();
		String base64Key = Base64.getEncoder().encodeToString(keyBytes);
		System.out.printf("Base64 Key: %s\n", base64Key);
	}
}
