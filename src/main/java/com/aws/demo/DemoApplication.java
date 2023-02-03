package com.aws.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.aws.demo.AWS.WorkFlowStarter;


@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
		System.out.println("Application started...");
		String[] arguments = {"Mahesh"};
		WorkFlowStarter.main(arguments);
	}

}
