package com.codelens.ai;

import com.codelens.ai.config.DotenvEnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CodeLensApplication {

	public static void main(String[] args) {
		DotenvEnvironmentPostProcessor.loadIntoSystemProperties();
		SpringApplication.run(CodeLensApplication.class, args);
	}
}
