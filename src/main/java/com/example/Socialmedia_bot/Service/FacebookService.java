package com.example.Socialmedia_bot.Service;

import com.example.Socialmedia_bot.model.ScheduledPost;
import com.restfb.BinaryAttachment;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.types.FacebookType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;

@Service
public class FacebookService {

    private static final Logger log = LoggerFactory.getLogger(FacebookService.class);

    private final FacebookClient facebookClient;
    private final String pageId;

    // Injected via @Bean in FacebookConfig
    public FacebookService(FacebookClient facebookClient, @Value("${facebook.page.id}") String pageId) {
        this.facebookClient = facebookClient;
        this.pageId = pageId;
        log.info("FacebookService initialized for Page ID: {}", pageId);
    }

    // === IMMEDIATE POSTING ===
    public String postMessage(String message) {
        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("Message cannot be empty");
        }
        try {
            FacebookType response = facebookClient.publish(
                    pageId + "/feed",
                    FacebookType.class,
                    Parameter.with("message", message)
            );
            String postId = response.getId();
            log.info("Text posted successfully. Post ID: {}", postId);
            return "Posted! ID: " + postId;
        } catch (Exception e) {
            log.error("Failed to post text: {}", e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    public String postImage(MultipartFile image, String message) throws IOException {
        validateImage(image);
        FacebookType response = facebookClient.publish(
                pageId + "/photos",
                FacebookType.class,
                BinaryAttachment.with(image.getOriginalFilename(), image.getInputStream()),
                Parameter.with("message", message != null ? message : ""),
                Parameter.with("published", true)
        );
        log.info("Image posted. Post ID: {}", response.getId());
        return "Image posted! ID: " + response.getId();
    }

    public String postVideo(MultipartFile video, String message) throws IOException {
        validateVideo(video);
        FacebookType response = facebookClient.publish(
                pageId + "/videos",
                FacebookType.class,
                BinaryAttachment.with(video.getOriginalFilename(), video.getInputStream()),
                Parameter.with("message", message != null ? message : ""),
                Parameter.with("published", true)
        );
        log.info("Video posted. Post ID: {}", response.getId());
        return "Video posted! ID: " + response.getId();
    }

    // === FOR SCHEDULER (byte[] input) ===
    public String publishScheduledPost(ScheduledPost post) {
        try {
            if ("image".equalsIgnoreCase(post.getMediaType()) && post.getMedia() != null) {
                return postImageFromBytes(post.getMedia(), post.getMessage(), "image.jpg");
            } else if ("video".equalsIgnoreCase(post.getMediaType()) && post.getMedia() != null) {
                return postVideoFromBytes(post.getMedia(), post.getMessage(), "video.mp4");
            } else {
                return postMessage(post.getMessage());
            }
        } catch (Exception e) {
            log.error("Failed to publish scheduled post ID {}: {}", post.getId(), e.getMessage());
            return "Failed: " + e.getMessage();
        }
    }

    private String postImageFromBytes(byte[] data, String message, String filename) {
        try {
            FacebookType response = facebookClient.publish(
                    pageId + "/photos",
                    FacebookType.class,
                    BinaryAttachment.with(filename, new ByteArrayInputStream(data)),
                    Parameter.with("message", message),
                    Parameter.with("published", true)
            );
            return "Image posted! ID: " + response.getId();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    private String postVideoFromBytes(byte[] data, String message, String filename) {
        try {
            FacebookType response = facebookClient.publish(
                    pageId + "/videos",
                    FacebookType.class,
                    BinaryAttachment.with(filename, new ByteArrayInputStream(data)),
                    Parameter.with("message", message),
                    Parameter.with("published", true)
            );
            return "Video posted! ID: " + response.getId();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    // === VALIDATION ===
    private void validateImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("Image required");
        if (!file.getContentType().startsWith("image/")) throw new IllegalArgumentException("Must be image");
    }

    private void validateVideo(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("Video required");
        if (!file.getContentType().startsWith("video/")) throw new IllegalArgumentException("Must be video");
    }
}