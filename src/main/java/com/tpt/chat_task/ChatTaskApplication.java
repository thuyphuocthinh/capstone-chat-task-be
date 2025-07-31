package com.tpt.chat_task;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class ChatTaskApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChatTaskApplication.class, args);
	}

}
