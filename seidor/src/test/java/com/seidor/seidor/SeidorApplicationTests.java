package com.seidor.seidor;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class SeidorApplicationTests {

	@Test
	void main_runsWithoutThrowing() {
		assertDoesNotThrow(() -> {
			SeidorApplication.main(new String[]{});
		});
	}

	@Test
	void seidorApplication_hasSpringBootApplicationAnnotation() {
		SpringBootApplication annotation =
				SeidorApplication.class.getAnnotation(SpringBootApplication.class);

		assertThat(annotation)
				.as("SeidorApplication should be annotated with @SpringBootApplication")
				.isNotNull();
	}
}
