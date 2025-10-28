package com.example.Socialmedia_bot.Service;


import com.example.Socialmedia_bot.model.ScheduledPost;
import com.example.Socialmedia_bot.Repository.ScheduledPostRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class PostScheduler {

    private static final Logger log = LoggerFactory.getLogger(PostScheduler.class);

    private final ScheduledPostRepository postRepo;
    private final FacebookService facebookService;

    public PostScheduler(ScheduledPostRepository postRepo, FacebookService facebookService) {
        this.postRepo = postRepo;
        this.facebookService = facebookService;
    }

    @Scheduled(fixedRateString = "${app.scheduler.interval}")
    public void publishDuePosts() {
        Instant now = Instant.now();
        List<ScheduledPost> due = postRepo.findByPostedFalseAndScheduledTimeBefore(now);

        log.info("Found {} post(s) ready to publish", due.size());

        due.forEach(post -> {
            try {
                String result = facebookService.publishScheduledPost(post);
                post.setFacebookPostId(extractPostId(result));
                post.setPosted(true);
                postRepo.save(post);
                log.info("Published post ID {} → {}", post.getId(), result);
            } catch (Exception e) {
                log.error("Failed to publish post ID {}: {}", post.getId(), e.getMessage());
            }
        });
    }

    private String extractPostId(String result) {
        return result.contains("Post ID:") ? result.split("Post ID:")[1].trim() : null;
    }
}