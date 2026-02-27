package com.programmingtechie.authservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SjcAuthApplication {
	public static void main(String[] args) {
		SpringApplication.run(SjcAuthApplication.class, args);
	}
}
