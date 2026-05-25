package com.fintech.billetera;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BilleteraApplication {

	public static void main(String[] args) {
		SpringApplication.run(BilleteraApplication.class, args);
	}

}
