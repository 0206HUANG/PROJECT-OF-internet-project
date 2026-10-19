package com.example.model;

import java.io.Serializable;
import java.util.Date;
import java.util.Locale;

// Information about a YouTube video, as returned by the YouTube Data API
public class YouTubeVideo implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String videoId;
    private final String title;
    private final String description;
    private final String channelTitle;
    private final Date publishedAt;
    // -1 when unknown
    private final long durationSeconds;
    private final long viewCount;
    private final long likeCount;

    public YouTubeVideo(String videoId, String title, String description, String channelTitle, Date publishedAt,
                        long durationSeconds, long viewCount, long likeCount) {
        this.videoId = videoId;
        this.title = title;
        this.description = description;
        this.channelTitle = channelTitle;
        this.publishedAt = publishedAt == null ? null : new Date(publishedAt.getTime());
        this.durationSeconds = durationSeconds;
        this.viewCount = viewCount;
        this.likeCount = likeCount;
    }

    // Only the id is known for sure, e.g. a video added by link while no API key is configured
    public static YouTubeVideo basic(String videoId, String title, String description) {
        return new YouTubeVideo(videoId, title, description, null, null, -1, -1, -1);
    }

    public static String watchUrl(String videoId) {
        return "https://www.youtube.com/watch?v=" + videoId;
    }

    // Privacy-enhanced embed: YouTube does not set cookies until the viewer plays the video
    public static String embedUrl(String videoId) {
        return "https://www.youtube-nocookie.com/embed/" + videoId;
    }

    public static String thumbnailUrl(String videoId) {
        return "https://i.ytimg.com/vi/" + videoId + "/mqdefault.jpg";
    }

    public String getVideoId() {
        return videoId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getChannelTitle() {
        return channelTitle;
    }

    public Date getPublishedAt() {
        return publishedAt == null ? null : new Date(publishedAt.getTime());
    }

    public long getDurationSeconds() {
        return durationSeconds;
    }

    public long getViewCount() {
        return viewCount;
    }

    public long getLikeCount() {
        return likeCount;
    }

    public String getWatchUrl() {
        return watchUrl(videoId);
    }

    public String getEmbedUrl() {
        return embedUrl(videoId);
    }

    public String getThumbnailUrl() {
        return thumbnailUrl(videoId);
    }

    // e.g. "4:05" or "1:02:03"; null for live streams or when unknown
    public String getDurationLabel() {
        if (durationSeconds <= 0) {
            return null;
        }
        long hours = durationSeconds / 3600;
        long minutes = durationSeconds % 3600 / 60;
        long seconds = durationSeconds % 60;
        return hours > 0
                ? String.format("%d:%02d:%02d", hours, minutes, seconds)
                : String.format("%d:%02d", minutes, seconds);
    }

    public String getViewCountLabel() {
        return viewCount < 0 ? null : String.format(Locale.US, "%,d", viewCount);
    }

    public String getLikeCountLabel() {
        return likeCount < 0 ? null : String.format(Locale.US, "%,d", likeCount);
    }
}
