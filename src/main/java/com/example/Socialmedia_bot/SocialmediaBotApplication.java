
package com.example.Socialmedia_bot;

import com.example.Socialmedia_bot.Service.FacebookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.time.ZoneId;

@SpringBootApplication
@EnableScheduling
public class SocialmediaBotApplication {

    private static final Logger log = LoggerFactory.getLogger(SocialmediaBotApplication.class);

    private final FacebookService facebookService;

    public SocialmediaBotApplication(FacebookService facebookService) {
        this.facebookService = facebookService;
    }

    public static void main(String[] args) {
        SpringApplication.run(SocialmediaBotApplication.class, args);
    }

    @Scheduled(fixedRateString = "${app.scheduler.interval:86400000}")
    public void checkScheduledPosts() {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("UTC"));
        log.info("=== SCHEDULER TRIGGERED AT {} (UTC) ===", now);
        try {
            facebookService.checkAndPostScheduledPosts();
            log.info("Scheduler check completed successfully.");
        } catch (Exception e) {
            log.error("Error during scheduled post check", e);
        }
    }
}
