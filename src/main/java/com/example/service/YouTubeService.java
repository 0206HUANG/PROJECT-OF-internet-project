package com.example.service;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.HtmlUtils;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.model.YouTubeSearchResult;
import com.example.model.YouTubeVideo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Talks to YouTube: recognises YouTube links and uses the YouTube Data API v3 to search videos,
 * read video details and list playlists.
 * The API needs a key, passed with -Dyoutube.api.key=... or the YOUTUBE_API_KEY environment variable.
 */
@Service
public class YouTubeService {

    private static final String API_URL = "https://www.googleapis.com/youtube/v3/";
    private static final int SEARCH_RESULTS_PER_PAGE = 12;
    // The API returns at most 50 items per request
    private static final int MAX_ITEMS_PER_REQUEST = 50;
    private static final int MAX_PLAYLIST_PAGES = 4;
    private static final Pattern VIDEO_ID = Pattern.compile("[A-Za-z0-9_-]{11}");
    private static final Pattern PLAYLIST_ID = Pattern.compile("[A-Za-z0-9_-]{12,64}");

    private final RestTemplate restTemplate;
    private final String apiKey;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public YouTubeService() {
        this(createRestTemplate(), System.getProperty("youtube.api.key", System.getenv("YOUTUBE_API_KEY")));
    }

    YouTubeService(RestTemplate restTemplate, String apiKey) {
        this.restTemplate = restTemplate;
        this.apiKey = apiKey == null ? null : apiKey.trim();
    }

    /**
     * Returns the 11-character video id from a YouTube link (watch, youtu.be, embed, shorts or live)
     * or from a bare video id, or null if the input is not a YouTube video.
     */
    public static String extractVideoId(String input) {
        if (input == null) {
            return null;
        }
        String value = input.trim();
        if (VIDEO_ID.matcher(value).matches()) {
            return value;
        }
        URI uri = parseYouTubeUri(value);
        if (uri == null) {
            return null;
        }
        String path = uri.getPath() == null ? "" : uri.getPath();

        String candidate = null;
        if (normalizedHost(uri).equals("youtu.be")) {
            candidate = firstSegment(path.substring(Math.min(1, path.length())));
        } else if (path.equals("/watch")) {
            candidate = queryParameter(uri.getRawQuery(), "v");
        } else {
            for (String prefix : new String[]{"/embed/", "/shorts/", "/live/", "/v/"}) {
                if (path.startsWith(prefix)) {
                    candidate = firstSegment(path.substring(prefix.length()));
                }
            }
        }
        return candidate != null && VIDEO_ID.matcher(candidate).matches() ? candidate : null;
    }

    // Returns the playlist id from a YouTube playlist link (or a bare playlist id), or null
    public static String extractPlaylistId(String input) {
        if (input == null) {
            return null;
        }
        String value = input.trim();
        if (PLAYLIST_ID.matcher(value).matches()) {
            return value;
        }
        URI uri = parseYouTubeUri(value);
        String candidate = uri == null ? null : queryParameter(uri.getRawQuery(), "list");
        return candidate != null && PLAYLIST_ID.matcher(candidate).matches() ? candidate : null;
    }

    public boolean isApiEnabled() {
        return StringUtils.hasText(apiKey);
    }

    // Searches embeddable videos; pageToken comes from a previous result (null for the first page)
    public YouTubeSearchResult search(String query, String pageToken) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("part", "snippet");
        params.put("type", "video");
        params.put("videoEmbeddable", "true");
        params.put("safeSearch", "strict");
        params.put("maxResults", SEARCH_RESULTS_PER_PAGE);
        params.put("q", query);
        if (StringUtils.hasText(pageToken)) {
            params.put("pageToken", pageToken);
        }
        JsonNode response = call("search", params);

        List<YouTubeVideo> videos = new ArrayList<>();
        for (JsonNode item : response.path("items")) {
            String videoId = item.path("id").path("videoId").asText("");
            if (!VIDEO_ID.matcher(videoId).matches()) {
                continue;
            }
            JsonNode snippet = item.path("snippet");
            // Search results contain HTML entities (e.g. &#39;); the views escape the text again when rendering
            videos.add(new YouTubeVideo(videoId,
                    HtmlUtils.htmlUnescape(snippet.path("title").asText("")),
                    HtmlUtils.htmlUnescape(snippet.path("description").asText("")),
                    HtmlUtils.htmlUnescape(snippet.path("channelTitle").asText("")),
                    parseDate(snippet.path("publishedAt").asText(null)), -1, -1, -1));
        }
        return new YouTubeSearchResult(videos,
                response.path("nextPageToken").asText(null), response.path("prevPageToken").asText(null));
    }

    // Full details (duration, views, likes...) of one video, or null if it does not exist or is private
    public YouTubeVideo getVideo(String videoId) {
        List<YouTubeVideo> videos = getVideos(Collections.singletonList(videoId));
        return videos.isEmpty() ? null : videos.get(0);
    }

    // Videos of a public or unlisted playlist (up to 200); private and deleted videos are skipped
    public List<YouTubeVideo> getPlaylistVideos(String playlistId) {
        List<YouTubeVideo> videos = new ArrayList<>();
        String pageToken = null;
        for (int page = 0; page < MAX_PLAYLIST_PAGES; page++) {
            Map<String, Object> params = new LinkedHashMap<>();
            params.put("part", "contentDetails");
            params.put("playlistId", playlistId);
            params.put("maxResults", MAX_ITEMS_PER_REQUEST);
            if (pageToken != null) {
                params.put("pageToken", pageToken);
            }
            JsonNode response = call("playlistItems", params);

            List<String> ids = new ArrayList<>();
            for (JsonNode item : response.path("items")) {
                String videoId = item.path("contentDetails").path("videoId").asText("");
                if (VIDEO_ID.matcher(videoId).matches()) {
                    ids.add(videoId);
                }
            }
            videos.addAll(getVideos(ids));

            pageToken = response.path("nextPageToken").asText(null);
            if (pageToken == null) {
                break;
            }
        }
        return videos;
    }

    private List<YouTubeVideo> getVideos(List<String> videoIds) {
        if (videoIds.isEmpty()) {
            return Collections.emptyList();
        }
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("part", "snippet,contentDetails,statistics");
        params.put("id", String.join(",", videoIds));
        params.put("maxResults", MAX_ITEMS_PER_REQUEST);
        JsonNode response = call("videos", params);

        List<YouTubeVideo> videos = new ArrayList<>();
        for (JsonNode item : response.path("items")) {
            String videoId = item.path("id").asText("");
            if (!VIDEO_ID.matcher(videoId).matches()) {
                continue;
            }
            JsonNode snippet = item.path("snippet");
            JsonNode statistics = item.path("statistics");
            videos.add(new YouTubeVideo(videoId,
                    snippet.path("title").asText(""),
                    snippet.path("description").asText(""),
                    snippet.path("channelTitle").asText(""),
                    parseDate(snippet.path("publishedAt").asText(null)),
                    parseDuration(item.path("contentDetails").path("duration").asText(null)),
                    statistics.path("viewCount").asLong(-1),
                    statistics.path("likeCount").asLong(-1)));
        }
        return videos;
    }

    private JsonNode call(String resource, Map<String, Object> params) {
        if (!isApiEnabled()) {
            throw new YouTubeException("The YouTube API is not configured: no API key has been set.");
        }
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(API_URL + resource);
        Map<String, Object> values = new LinkedHashMap<>(params);
        values.put("key", apiKey);
        for (String name : values.keySet()) {
            builder.queryParam(name, "{" + name + "}");
        }
        // Encoding the values as template variables also encodes characters such as '+' and '&'
        URI uri = builder.encode().buildAndExpand(values).toUri();

        try {
            return readJson(restTemplate.getForObject(uri, String.class));
        } catch (HttpStatusCodeException e) {
            throw new YouTubeException("YouTube request failed: " + apiErrorMessage(e));
        } catch (ResourceAccessException e) {
            throw new YouTubeException("Could not connect to YouTube. Please check the network connection.");
        } catch (RestClientException e) {
            throw new YouTubeException("YouTube request failed. Please try again later.");
        }
    }

    private String apiErrorMessage(HttpStatusCodeException e) {
        JsonNode error;
        try {
            error = readJson(e.getResponseBodyAsString()).path("error");
        } catch (YouTubeException notJson) {
            return "HTTP " + e.getRawStatusCode();
        }
        if ("quotaExceeded".equals(error.path("errors").path(0).path("reason").asText(""))) {
            return "the daily YouTube API quota has been used up. Please try again tomorrow.";
        }
        String message = error.path("message").asText("");
        return StringUtils.hasText(message) ? message : "HTTP " + e.getRawStatusCode();
    }

    private JsonNode readJson(String body) {
        try {
            return objectMapper.readTree(StringUtils.hasText(body) ? body : "{}");
        } catch (IOException e) {
            throw new YouTubeException("YouTube returned an unexpected response.");
        }
    }

    private static Date parseDate(String value) {
        try {
            return value == null ? null : Date.from(Instant.parse(value));
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    // YouTube durations are ISO-8601, e.g. "PT1H2M3S"
    private static long parseDuration(String value) {
        try {
            return value == null ? -1 : Duration.parse(value).getSeconds();
        } catch (DateTimeParseException e) {
            return -1;
        }
    }

    private static URI parseYouTubeUri(String value) {
        try {
            URI uri = new URI(value.contains("://") ? value : "https://" + value);
            String host = normalizedHost(uri);
            boolean youtube = host.equals("youtube.com") || host.equals("youtube-nocookie.com")
                    || host.equals("youtu.be");
            return youtube ? uri : null;
        } catch (URISyntaxException e) {
            return null;
        }
    }

    private static String normalizedHost(URI uri) {
        String host = uri.getHost() == null ? "" : uri.getHost().toLowerCase(Locale.ROOT);
        if (host.startsWith("www.") || host.startsWith("m.")) {
            host = host.substring(host.indexOf('.') + 1);
        }
        return host;
    }

    private static String firstSegment(String path) {
        int slash = path.indexOf('/');
        return slash < 0 ? path : path.substring(0, slash);
    }

    private static String queryParameter(String rawQuery, String name) {
        if (rawQuery == null) {
            return null;
        }
        for (String pair : rawQuery.split("&")) {
            if (pair.startsWith(name + "=")) {
                return pair.substring(name.length() + 1);
            }
        }
        return null;
    }

    private static RestTemplate createRestTemplate() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(5000);
        requestFactory.setReadTimeout(10000);
        return new RestTemplate(requestFactory);
    }

    // Carries a message that can be shown to the user
    public static class YouTubeException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        public YouTubeException(String message) {
            super(message);
        }
    }
}
