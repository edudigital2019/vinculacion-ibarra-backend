package com.app.municipio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class MunicipioApplication {

	public static void main(String[] args) {
		SpringApplication.run(MunicipioApplication.class, args);
	}

}
