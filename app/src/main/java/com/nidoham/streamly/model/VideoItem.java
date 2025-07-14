package com.nidoham.streamly.model;

public class VideoItem {
    private String id;
    private String title;
    private String thumbnailUrl;
    private String channelName;
    private String channelLogoUrl;
    private String duration;
    private String uploadTime;
    private String viewCount;
    private boolean hasTextOverlay;
    private String overlayText;
    
    // Constructors
    public VideoItem() {}
    
    public VideoItem(String id, String title, String thumbnailUrl, String channelName, 
                    String channelLogoUrl, String duration, String uploadTime, String viewCount) {
        this.id = id;
        this.title = title;
        this.thumbnailUrl = thumbnailUrl;
        this.channelName = channelName;
        this.channelLogoUrl = channelLogoUrl;
        this.duration = duration;
        this.uploadTime = uploadTime;
        this.viewCount = viewCount;
        this.hasTextOverlay = false;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }
    
    public String getChannelName() { return channelName; }
    public void setChannelName(String channelName) { this.channelName = channelName; }
    
    public String getChannelLogoUrl() { return channelLogoUrl; }
    public void setChannelLogoUrl(String channelLogoUrl) { this.channelLogoUrl = channelLogoUrl; }
    
    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }
    
    public String getUploadTime() { return uploadTime; }
    public void setUploadTime(String uploadTime) { this.uploadTime = uploadTime; }
    
    public String getViewCount() { return viewCount; }
    public void setViewCount(String viewCount) { this.viewCount = viewCount; }
    
    public boolean hasTextOverlay() { return hasTextOverlay; }
    public void setHasTextOverlay(boolean hasTextOverlay) { this.hasTextOverlay = hasTextOverlay; }
    
    public String getOverlayText() { return overlayText; }
    public void setOverlayText(String overlayText) { 
        this.overlayText = overlayText; 
        this.hasTextOverlay = overlayText != null && !overlayText.isEmpty();
    }
}