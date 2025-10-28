package com.example.Socialmedia_bot.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "scheduled_posts")
public class ScheduledPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Lob                     // stores large binary data
    private byte[] media;

    @Column(name = "media_type")
    private String mediaType;               // "text", "image", "video"

    @Column(name = "media_content_type")
    private String mediaContentType;        // e.g. "image/jpeg"

    @Column(name = "scheduled_time", nullable = false)
    private Instant scheduledTime;          // UTC instant – safe across time-zones

    @Column(name = "posted", nullable = false)
    private boolean posted = false;

    @Column(name = "facebook_post_id")
    private String facebookPostId;

    /* ------------------- Getters & Setters ------------------- */
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public byte[] getMedia() { return media; }
    public void setMedia(byte[] media) { this.media = media; }

    public String getMediaType() { return mediaType; }
    public void setMediaType(String mediaType) { this.mediaType = mediaType; }

    public String getMediaContentType() { return mediaContentType; }
    public void setMediaContentType(String mediaContentType) { this.mediaContentType = mediaContentType; }

    public Instant getScheduledTime() { return scheduledTime; }
    public void setScheduledTime(Instant scheduledTime) { this.scheduledTime = scheduledTime; }

    public boolean isPosted() { return posted; }
    public void setPosted(boolean posted) { this.posted = posted; }

    public String getFacebookPostId() { return facebookPostId; }
    public void setFacebookPostId(String facebookPostId) { this.facebookPostId = facebookPostId; }
}