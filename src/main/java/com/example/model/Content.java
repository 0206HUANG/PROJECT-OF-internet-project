package com.example.model;

import java.io.Serializable;
import java.util.Date;

public class Content implements Serializable {

    private static final long serialVersionUID = 1L;

    private final long id;
    private final String title;
    private final String description;
    private final String originalFilename;
    private final String storedFilename;
    private final String contentType;
    private final long size;
    private final String uploadedBy;
    private final String uploaderName;
    private final Date uploadedAt;

    public Content(long id, String title, String description, String originalFilename, String storedFilename,
                   String contentType, long size, String uploadedBy, String uploaderName, Date uploadedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.originalFilename = originalFilename;
        this.storedFilename = storedFilename;
        this.contentType = contentType;
        this.size = size;
        this.uploadedBy = uploadedBy;
        this.uploaderName = uploaderName;
        this.uploadedAt = new Date(uploadedAt.getTime());
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

    public String getUploadedBy() {
        return uploadedBy;
    }

    public String getUploaderName() {
        return uploaderName;
    }

    public Date getUploadedAt() {
        return new Date(uploadedAt.getTime());
    }

    public boolean isVideo() {
        return contentType.startsWith("video/");
    }

    // Human-readable file size for the views, e.g. "2.4 MB"
    public String getSizeLabel() {
        if (size < 1024) {
            return size + " B";
        }
        if (size < 1024 * 1024) {
            return String.format("%.1f KB", size / 1024.0);
        }
        return String.format("%.1f MB", size / (1024.0 * 1024.0));
    }
}
