package com.example.Socialmedia_bot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SocialmediaBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(SocialmediaBotApplication.class, args);
    }
}
