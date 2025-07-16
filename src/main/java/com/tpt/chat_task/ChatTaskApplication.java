package com.tpt.chat_task;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
@EnableRabbit
public class ChatTaskApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChatTaskApplication.class, args);
	}

}
