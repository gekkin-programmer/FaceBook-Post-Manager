package com.example.Socialmedia_bot.Service;


import com.example.Socialmedia_bot.model.ScheduledPost;
import com.restfb.BinaryAttachment;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.types.FacebookType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
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

    public String schedulePost(String message, MultipartFile media, String mediaType, LocalDateTime scheduledTime) {
        try {
            ScheduledPost post = new ScheduledPost();
            post.setId(idGenerator.getAndIncrement());
            post.setMessage(message);
            post.setMediaType(mediaType);
            // Ensure scheduledTime is interpreted in WAT (Africa/Lagos) timezone
            post.setScheduledTime(scheduledTime.atZone(ZoneId.of("Africa/Lagos")).toLocalDateTime());
            post.setPosted(false);

            if (media != null && !media.isEmpty()) {
                if ("image".equalsIgnoreCase(mediaType) && !media.getContentType().startsWith("image/")) {
                    throw new IllegalArgumentException("File must be an image (e.g., JPEG, PNG)");
                } else if ("video".equalsIgnoreCase(mediaType) && !media.getContentType().startsWith("video/")) {
                    throw new IllegalArgumentException("File must be a video (e.g., MP4, MOV)");
                }
                post.setMedia(media.getBytes());
            } else if ("image".equalsIgnoreCase(mediaType) || "video".equalsIgnoreCase(mediaType)) {
                throw new IllegalArgumentException("Media file required for " + mediaType + " post");
            }

            synchronized (scheduledPosts) {
                scheduledPosts.add(post);
            }
            return "Post scheduled successfully! ID: " + post.getId() + ", Scheduled Time: " + scheduledTime + " WAT";
        } catch (IOException e) {
            e.printStackTrace();
            return "Error scheduling post: " + e.getMessage();
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