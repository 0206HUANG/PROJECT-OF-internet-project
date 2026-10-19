package com.example.model;

import java.io.Serializable;
import java.util.Date;

// An item in the content library: either an uploaded file or a YouTube video
public class Content implements Serializable {

    private static final long serialVersionUID = 1L;

    private final long id;
    private final String title;
    private final String description;
    private final String uploadedBy;
    private final String uploaderName;
    private final Date uploadedAt;

    // Only set for uploaded files
    private final String originalFilename;
    private final String storedFilename;
    private final String contentType;
    private final long size;

    // Only set for YouTube videos
    private final YouTubeVideo youtubeVideo;

    private Content(long id, String title, String description, String uploadedBy, String uploaderName,
                    Date uploadedAt, String originalFilename, String storedFilename, String contentType, long size,
                    YouTubeVideo youtubeVideo) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.uploadedBy = uploadedBy;
        this.uploaderName = uploaderName;
        this.uploadedAt = new Date(uploadedAt.getTime());
        this.originalFilename = originalFilename;
        this.storedFilename = storedFilename;
        this.contentType = contentType;
        this.size = size;
        this.youtubeVideo = youtubeVideo;
    }

    public static Content uploadedFile(long id, String title, String description, String originalFilename,
                                       String storedFilename, String contentType, long size,
                                       User uploader, Date uploadedAt) {
        return new Content(id, title, description, uploader.getUsername(), uploader.getFullName(), uploadedAt,
                originalFilename, storedFilename, contentType, size, null);
    }

    public static Content youtubeVideo(long id, String title, String description, YouTubeVideo video,
                                       User uploader, Date uploadedAt) {
        return new Content(id, title, description, uploader.getUsername(), uploader.getFullName(), uploadedAt,
                null, null, null, 0, video);
    }

    // Copy of this item with a new title and description
    public Content withDetails(String newTitle, String newDescription) {
        return new Content(id, newTitle, newDescription, uploadedBy, uploaderName, uploadedAt,
                originalFilename, storedFilename, contentType, size, youtubeVideo);
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getUploadedBy() {
        return uploadedBy;
    }

    public String getUploaderName() {
        return uploaderName;
    }

    public Date getUploadedAt() {
        return new Date(uploadedAt.getTime());
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public String getStoredFilename() {
        return storedFilename;
    }

    public String getContentType() {
        return contentType;
    }

    public long getSize() {
        return size;
    }

    public YouTubeVideo getYoutubeVideo() {
        return youtubeVideo;
    }

    public boolean isYoutube() {
        return youtubeVideo != null;
    }

    public boolean isVideo() {
        return isYoutube() || contentType.startsWith("video/");
    }

    public String getTypeLabel() {
        if (isYoutube()) {
            return "YouTube";
        }
        return isVideo() ? "Video" : "Image";
    }

    // Human-readable file size for the views, e.g. "2.4 MB"
    public String getSizeLabel() {
        if (isYoutube()) {
            return "-";
        }
        if (size < 1024) {
            return size + " B";
        }
        if (size < 1024 * 1024) {
            return String.format("%.1f KB", size / 1024.0);
        }
        return String.format("%.1f MB", size / (1024.0 * 1024.0));
    }

    public String getYoutubeVideoId() {
        return isYoutube() ? youtubeVideo.getVideoId() : null;
    }

    public String getYoutubeWatchUrl() {
        return isYoutube() ? youtubeVideo.getWatchUrl() : null;
    }

    public String getYoutubeEmbedUrl() {
        return isYoutube() ? youtubeVideo.getEmbedUrl() : null;
    }

    public String getYoutubeThumbnailUrl() {
        return isYoutube() ? youtubeVideo.getThumbnailUrl() : null;
    }
}
