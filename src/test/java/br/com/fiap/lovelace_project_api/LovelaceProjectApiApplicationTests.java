package br.com.fiap.lovelace_project_api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class LovelaceProjectApiApplicationTests {

	@Test
	void contextLoads() {
	}

}
