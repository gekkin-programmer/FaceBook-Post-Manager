package com.example.Socialmedia_bot;

import com.example.Socialmedia_bot.Service.FacebookService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@SpringBootApplication
@EnableScheduling
public class SocialmediaBotApplication {
    private final FacebookService facebookService;

    public SocialmediaBotApplication(FacebookService facebookService) {
        this.facebookService = facebookService;
    }

	public static void main(String[] args) {
		SpringApplication.run(SocialmediaBotApplication.class, args);
	}

    @Scheduled(fixedRate = 86400000)
    public void checkScheduledPosts(){
        facebookService.checkAndPostScheduledPosts();
    }
}
