package com.aloneinabyss.lovelace;

import org.springframework.boot.SpringApplication;

public class TestLovelaceProjectApiApplication {

	public static void main(String[] args) {
		SpringApplication.from(LovelaceProjectApiApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
