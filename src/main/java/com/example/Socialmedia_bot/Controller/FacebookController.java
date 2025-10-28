package com.example.Socialmedia_bot.Controller;

import com.example.Socialmedia_bot.Service.FacebookService;
import com.example.Socialmedia_bot.model.ScheduledPost;
import com.example.Socialmedia_bot.Repository.ScheduledPostRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@RestController
@RequestMapping("/api/facebook")
public class FacebookController {

    private static final Logger log = LoggerFactory.getLogger(FacebookController.class);
    private static final ZoneId WAT = ZoneId.of("Africa/Lagos");
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final FacebookService facebookService;
    private final ScheduledPostRepository postRepo;

    public FacebookController(FacebookService facebookService, ScheduledPostRepository postRepo) {
        this.facebookService = facebookService;
        this.postRepo = postRepo;
        log.info("FacebookController initialized");
    }

    // === IMMEDIATE POSTS ===
    @PostMapping("/post")
    public ResponseEntity<String> postText(@RequestParam String message) {
        if (message == null || message.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Message is required");
        }
        try {
            String result = facebookService.postMessage(message.trim());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Failed to post text: {}", e.getMessage());
            return ResponseEntity.status(500).body("Failed to post: " + e.getMessage());
        }
    }

    @PostMapping(value = "/post/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> postImage(
            @RequestPart("image") MultipartFile image,
            @RequestPart(value = "message", required = false) String message
    ) {
        try {
            String result = facebookService.postImage(image, message);
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Upload failed: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping(value = "/post/video", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> postVideo(
            @RequestPart("video") MultipartFile video,
            @RequestPart(value = "message", required = false) String message
    ) {
        try {
            String result = facebookService.postVideo(video, message);
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Upload failed: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // === SCHEDULING ===
    @PostMapping(value = "/schedule", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> schedulePost(
            @RequestPart(value = "message", required = false) String message,
            @RequestPart(value = "media", required = false) MultipartFile media,
            @RequestPart(value = "mediaType", required = false) String mediaType,
            @RequestPart("scheduledTime") String scheduledTimeStr
    ) {
        try {
            // Parse ISO datetime (e.g., "2025-10-28T18:00:00")
            LocalDateTime watTime = LocalDateTime.parse(scheduledTimeStr, ISO_FORMATTER);
            Instant utcTime = watTime.atZone(WAT).toInstant();

            if (utcTime.isBefore(Instant.now())) {
                return ResponseEntity.badRequest().body("Scheduled time must be in the future (WAT)");
            }

            ScheduledPost post = new ScheduledPost();
            post.setMessage(message);
            post.setScheduledTime(utcTime);
            post.setPosted(false);

            if (media != null && !media.isEmpty()) {
                if (!"image".equalsIgnoreCase(mediaType) && !"video".equalsIgnoreCase(mediaType)) {
                    return ResponseEntity.badRequest().body("mediaType must be 'image' or 'video'");
                }
                post.setMediaType(mediaType.toLowerCase());
                post.setMediaContentType(media.getContentType());
                post.setMedia(media.getBytes());
            } else if (mediaType != null) {
                return ResponseEntity.badRequest().body("Media file required when mediaType is specified");
            } else {
                post.setMediaType("text");
            }

            ScheduledPost saved = postRepo.save(post);
            log.info("Scheduled post ID {} for {} WAT", saved.getId(), watTime);

            return ResponseEntity.ok(
                    "Post scheduled! ID: " + saved.getId() +
                            ", Time: " + watTime + " WAT (" + utcTime.atZone(WAT).toLocalDateTime() + ")"
            );

        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest().body("Invalid date format. Use: YYYY-MM-DDTHH:MM:SS (e.g., 2025-10-28T18:00:00)");
        } catch (IOException e) {
            return ResponseEntity.status(500).body("File processing failed: " + e.getMessage());
        } catch (Exception e) {
            log.error("Scheduling failed", e);
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    // === LIST SCHEDULED POSTS ===
    @GetMapping("/scheduled")
    public ResponseEntity<?> getScheduledPosts() {
        try {
            var posts = postRepo.findAll().stream()
                    .map(post -> {
                        String watTime = post.getScheduledTime().atZone(WAT).format(ISO_FORMATTER);
                        return String.format("ID: %d | %s WAT | %s | Posted: %s",
                                post.getId(), watTime, post.getMessage(), post.isPosted());
                    })
                    .toList();
            return ResponseEntity.ok(posts);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error fetching posts");
        }
    }
}