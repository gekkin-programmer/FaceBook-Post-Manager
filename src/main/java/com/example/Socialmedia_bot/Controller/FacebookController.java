package com.example.Socialmedia_bot.Controller;


import com.example.Socialmedia_bot.Service.FacebookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@RestController
public class FacebookController {

    private final FacebookService facebookService;

    @Autowired
    public FacebookController(FacebookService facebookService) {
        this.facebookService = facebookService;
        System.out.println("Initializing FacebookController with FacebookService: " + (facebookService != null ? "present" : "null"));
    }

    @PostMapping("/post-to-facebook")
    public String postToFacebook(@RequestParam String message) {
        return facebookService.postMessage(message);
    }

    @GetMapping("/post-to-facebook")
    public String getPostToFacebook(@RequestParam String message) {
        return facebookService.postMessage(message);
    }

    @PostMapping(value = "/post-image", consumes = "multipart/form-data")
    public String postImage(
            @RequestPart("image") MultipartFile image,
            @RequestPart(value = "message", required = false) String message
    ) {
        return facebookService.postImage(image, message);
    }

    @PostMapping(value = "/post-video", consumes = "multipart/form-data")
    public String postVideo(
            @RequestPart("video") MultipartFile video,
            @RequestPart(value = "message", required = false) String message
    ) {
        return facebookService.postVideo(video, message);
    }

    @PostMapping(value = "/schedule-post", consumes = "multipart/form-data")
    public String schedulePost(
            @RequestPart(value = "message", required = false) String message,
            @RequestPart(value = "media", required = false) MultipartFile media,
            @RequestPart(value = "mediaType", required = false) String mediaType,
            @RequestPart("scheduledTime") String scheduledTime
    ) {
        try {
            LocalDateTime scheduledDateTime = LocalDateTime.parse(scheduledTime);
            if (scheduledDateTime.isBefore(LocalDateTime.now())) {
                throw new IllegalArgumentException("Scheduled time must be in the future");
            }
            return facebookService.schedulePost(message, media, mediaType, scheduledDateTime);
        } catch (Exception e) {
            return "Error scheduling post: " + e.getMessage();
        }
    }
}