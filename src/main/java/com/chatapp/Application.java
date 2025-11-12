/*  How to compile: 
 * ./mvnw clean compile 
 * 
 * How to run: 
 * 1. Navigate to the project directory (cd 3640_project)
 * 2. Run " mvn spring-boot:run " to start the application	
 * 3. Open a web browser and go to " http://localhost:8080/test-chat.html " to test the chat functionality
 * 4. Open multiple browser tabs to simulate different users and test real-time messaging
 */

package com.chatapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
