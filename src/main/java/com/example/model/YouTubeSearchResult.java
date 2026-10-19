package com.example.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

// One page of YouTube search results
public class YouTubeSearchResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private final List<YouTubeVideo> videos;
    private final String nextPageToken;
    private final String prevPageToken;

    public YouTubeSearchResult(List<YouTubeVideo> videos, String nextPageToken, String prevPageToken) {
        this.videos = new ArrayList<>(videos);
        this.nextPageToken = nextPageToken;
        this.prevPageToken = prevPageToken;
    }

    public List<YouTubeVideo> getVideos() {
        return new ArrayList<>(videos);
    }

    public String getNextPageToken() {
        return nextPageToken;
    }

    public String getPrevPageToken() {
        return prevPageToken;
    }
}
