package com.example.Socialmedia_bot.model;


import java.time.LocalDateTime;

public class ScheduledPost {
    private Long id;
    private String message;
    private byte[] media;
    private String mediaType;
    private String mediaContentType;
    private LocalDateTime scheduledTime;
    private boolean posted;
    private String facebookPostId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public byte[] getMedia() {
        return media;
    }

    public void setMedia(byte[] media) {
        this.media = media;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public LocalDateTime getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(LocalDateTime scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public boolean isPosted() {
        return posted;
    }

    public void setPosted(boolean posted) {
        this.posted = posted;
    }

    public void setMediaContentType(String mediaContentType) { this.mediaContentType = mediaContentType; }

    public void setFacebookPostId(String facebookPostId) { this.facebookPostId = facebookPostId; }

    public String getMediaContentType() {
    }

    public String getFacebookPostId() {
    }
}