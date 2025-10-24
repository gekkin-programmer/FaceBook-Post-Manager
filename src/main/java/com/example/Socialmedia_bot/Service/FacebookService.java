package com.example.Socialmedia_bot.Service;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import com.example.Socialmedia_bot.model.ScheduledPost;
import com.restfb.BinaryAttachment;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.types.FacebookType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class FacebookService {

    private final FacebookClient facebookClient;
    private final List<ScheduledPost> scheduledPosts = new ArrayList<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Value("${facebook.page.id:}")
    private String pageId;

    public FacebookService(FacebookClient facebookClient) {
        this.facebookClient = facebookClient;

// 1. Try -D system property
        String sysId = System.getProperty("facebook.page.id");
        if (sysId != null && !sysId.isEmpty()) {
            this.pageId = sysId;
        }
        // 2. Try @Value from properties
        else if (this.pageId == null || this.pageId.isEmpty()) {
            this.pageId = System.getenv("FACEBOOK_PAGE_ID");
        }

        if (pageId == null || pageId.isEmpty()) {
            throw new IllegalStateException(
                    "facebook.page.id is not set!\n" +
                            "  • Use: java -Dfacebook.page.id=112408564785128 ...\n" +
                            "  • Or set env: FACEBOOK_PAGE_ID=112408564785128\n" +
                            "  • Or hardcode in application.properties"
            );
        }
        System.out.println("Initializing FacebookService with FacebookClient: " + (facebookClient != null ? "present" : "null"));
        System.out.println("Page ID: " + (pageId != null ? pageId : "null"));
    }

    public String postMessage(String message) {
        try {
            if (message == null || message.trim().isEmpty()) {
                throw new IllegalArgumentException("Message cannot be empty");
            }
            FacebookType response = facebookClient.publish(
                    pageId + "/feed",
                    FacebookType.class,
                    Parameter.with("message", message)
            );
            return "Posted successfully! Post ID: " + response.getId();
        } catch (Exception e) {
            e.printStackTrace();
            return "Error posting to Facebook: " + e.getMessage();
        }
    }

    public String postImage(MultipartFile image, String message) {
        try {
            if (image == null || image.isEmpty()) {
                throw new IllegalArgumentException("Image file cannot be empty");
            }
            if (!image.getContentType().startsWith("image/")) {
                throw new IllegalArgumentException("File must be an image (e.g., JPEG, PNG)");
            }
            FacebookType response = facebookClient.publish(
                    pageId + "/photos",
                    FacebookType.class,
                    BinaryAttachment.with(image.getOriginalFilename(), image.getInputStream()),
                    Parameter.with("message", message != null ? message : ""),
                    Parameter.with("published", true)
            );
            return "Image posted successfully! Post ID: " + response.getId();
        } catch (IOException e) {
            e.printStackTrace();
            return "Error uploading image: " + e.getMessage();
        } catch (Exception e) {
            e.printStackTrace();
            return "Error posting image to Facebook: " + e.getMessage();
        }
    }

    public String postVideo(MultipartFile video, String message) {
        try {
            if (video == null || video.isEmpty()) {
                throw new IllegalArgumentException("Video file cannot be empty");
            }
            if (!video.getContentType().startsWith("video/")) {
                throw new IllegalArgumentException("File must be a video (e.g., MP4, MOV)");
            }
            FacebookType response = facebookClient.publish(
                    pageId + "/videos",
                    FacebookType.class,
                    BinaryAttachment.with(video.getOriginalFilename(), video.getInputStream()),
                    Parameter.with("message", message != null ? message : ""),
                    Parameter.with("published", true)
            );
            return "Video posted successfully! Post ID: " + response.getId();
        } catch (IOException e) {
            e.printStackTrace();
            return "Error uploading video: " + e.getMessage();
        } catch (Exception e) {
            e.printStackTrace();
            return "Error posting video to Facebook: " + e.getMessage();
        }
    }

    @Component
    public class ScheduledPostExecutor {
        private final RestTemplate restTemplate = new RestTemplate();
        @Value("${facebook.page.access-token}")
        private String pageAccessToken;
        @Value("${facebook.page.id}")
        private String pageId;

        public void executeScheduledPost(ScheduledPost post) {
            if (post.isPosted() || post.getScheduledTime().isAfter(LocalDateTime.now(ZoneId.of("Africa/Lagos")))) {
                System.out.println("Skipping post ID " + post.getId() + ": Already posted or not due yet.");
                return;
            }

            try {
                String photoId = null;
                // Step 1: Upload image to /photos endpoint if present
                if (post.getMedia() != null && post.getMedia().length > 0 && "image".equalsIgnoreCase(post.getMediaType())) {
                    if (post.getMediaContentType() == null || !post.getMediaContentType().startsWith("image/")) {
                        throw new IllegalStateException("Invalid image content type: " + post.getMediaContentType());
                    }

                    String photoUrl = "https://graph.facebook.com/v19.0/" + pageId + "/photos";
                    HttpHeaders photoHeaders = new HttpHeaders();
                    photoHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
                    MultiValueMap<String, Object> photoBody = new LinkedMultiValueMap<>();

                    // Image upload
                    HttpHeaders imageHeaders = new HttpHeaders();
                    imageHeaders.setContentType(MediaType.parseMediaType(post.getMediaContentType()));
                    ByteArrayResource imageResource = new ByteArrayResource(post.getMedia()) {
                        @Override
                        public String getFilename() {
                            return "post-image." + post.getMediaContentType().split("/")[1];
                        }
                    };
                    photoBody.add("source", imageResource);
                    photoBody.add("access_token", pageAccessToken);
                    photoBody.add("published", "false"); // Unpublished photo for scheduled post

                    HttpEntity<MultiValueMap<String, Object>> photoRequest = new HttpEntity<>(photoBody, photoHeaders);
                    ResponseEntity<Map> photoResponse = restTemplate.postForEntity(photoUrl, photoRequest, Map.class);

                    if (photoResponse.getStatusCode() == HttpStatus.OK && photoResponse.getBody().containsKey("id")) {
                        photoId = photoResponse.getBody().get("id").toString();
                        System.out.println("Image uploaded successfully, Photo ID: " + photoId);
                    } else {
                        throw new RuntimeException("Failed to upload image: " + photoResponse.getBody());
                    }
                }

                // Step 2: Schedule the post with message and optional photo ID
                String feedUrl = "https://graph.facebook.com/v19.0/" + pageId + "/feed";
                HttpHeaders feedHeaders = new HttpHeaders();
                feedHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                MultiValueMap<String, Object> feedBody = new LinkedMultiValueMap<>();

                // Add message
                feedBody.add("message", post.getMessage());

                // Add photo ID if available
                if (photoId != null) {
                    feedBody.add("object_attachment", photoId); // Attach uploaded photo
                }

                // Scheduling (WAT to UTC Unix timestamp)
                long scheduledUnixTime = post.getScheduledTime()
                        .atZone(ZoneId.of("Africa/Lagos"))
                        .withZoneSameInstant(ZoneId.of("UTC"))
                        .toInstant()
                        .toEpochMilli() / 1000;
                feedBody.add("scheduled_publish_time", scheduledUnixTime);
                feedBody.add("published", "false"); // Schedule the post
                feedBody.add("access_token", pageAccessToken);
                feedBody.add("privacy", "{\"value\":\"EVERYONE\"}");

                HttpEntity<MultiValueMap<String, Object>> feedRequest = new HttpEntity<>(feedBody, feedHeaders);
                ResponseEntity<Map> feedResponse = restTemplate.postForEntity(feedUrl, feedRequest, Map.class);

                if (feedResponse.getStatusCode() == HttpStatus.OK) {
                    post.setPosted(true);
                    post.setFacebookPostId(feedResponse.getBody().get("id").toString());
                    System.out.println("Post scheduled successfully! Facebook Post ID: " + post.getFacebookPostId() +
                            ", Scheduled Time: " + post.getScheduledTime() + " WAT");
                } else {
                    System.err.println("Facebook API Error (Feed): " + feedResponse.getBody());
                }
            } catch (HttpClientErrorException e) {
                System.err.println("Facebook API Error: Status " + e.getStatusCode() + ", Response: " + e.getResponseBodyAsString());
                e.printStackTrace();
            } catch (Exception e) {
                System.err.println("Error scheduling post to Facebook: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }


    private static final Logger log = LoggerFactory.getLogger(FacebookService.class);

    public void checkAndPostScheduledPosts() {
        ZoneId wat = ZoneId.of("Africa/Lagos");
        LocalDateTime now = LocalDateTime.now(wat);
        log.info("CHECKING POSTS – NOW: {} (WAT)", now);

        synchronized (scheduledPosts) {
            List<ScheduledPost> toRemove = new ArrayList<>();
            for (ScheduledPost p : scheduledPosts) {
                if (!p.isPosted() && !now.isBefore(p.getScheduledTime())) {
                    log.info("POSTING ID {}: {}", p.getId(), p.getMessage());
                    String result = postMessage(p.getMessage());
                    log.info("RESULT: {}", result);
                    p.setPosted(true);
                    toRemove.add(p);
                } else {
                    log.debug("SKIPPED ID {} – scheduled: {}", p.getId(), p.getScheduledTime());
                }
            }
            scheduledPosts.removeAll(toRemove);
        }
    }

    private String postImageFromBytes(byte[] imageData, String message) {
        try {
            FacebookType response = facebookClient.publish(
                    pageId + "/photos",
                    FacebookType.class,
                    BinaryAttachment.with("image.jpg", new ByteArrayInputStream(imageData)),
                    Parameter.with("message", message != null ? message : ""),
                    Parameter.with("published", true)
            );
            return "Image posted successfully! Post ID: " + response.getId();
        } catch (Exception e) {
            e.printStackTrace();
            return "Error posting image to Facebook: " + e.getMessage();
        }
    }

    private String postVideoFromBytes(byte[] videoData, String message) {
        try {
            FacebookType response = facebookClient.publish(
                    pageId + "/videos",
                    FacebookType.class,
                    BinaryAttachment.with("video.mp4", new ByteArrayInputStream(videoData)),
                    Parameter.with("message", message != null ? message : ""),
                    Parameter.with("published", true)
            );
            return "Video posted successfully! Post ID: " + response.getId();
        } catch (Exception e) {
            e.printStackTrace();
            return "Error posting video to Facebook: " + e.getMessage();
        }
    }
}