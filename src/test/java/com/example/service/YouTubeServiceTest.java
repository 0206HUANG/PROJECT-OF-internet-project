package com.example.service;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import com.example.model.YouTubeSearchResult;
import com.example.model.YouTubeVideo;
import com.example.service.YouTubeService.YouTubeException;

public class YouTubeServiceTest {

    private static final String PLAYLIST_ID = "PLx0sYbCqOb8TBPRdmBHs5Iftvv9TPboYG";

    private RestTemplate restTemplate;
    private MockRestServiceServer youtube;
    private YouTubeService service;

    @Before
    public void setUp() {
        restTemplate = new RestTemplate();
        youtube = MockRestServiceServer.bindTo(restTemplate).build();
        service = new YouTubeService(restTemplate, "test-key");
    }

    @After
    public void verifyRequests() {
        youtube.verify();
    }

    @Test
    public void extractsVideoIdsFromCommonLinks() {
        String id = "dQw4w9WgXcQ";
        assertEquals(id, YouTubeService.extractVideoId("https://www.youtube.com/watch?v=dQw4w9WgXcQ"));
        assertEquals(id, YouTubeService.extractVideoId("https://youtu.be/dQw4w9WgXcQ?t=42"));
        assertEquals(id, YouTubeService.extractVideoId("youtube.com/watch?feature=share&v=dQw4w9WgXcQ"));
        assertEquals(id, YouTubeService.extractVideoId("https://m.youtube.com/watch?v=dQw4w9WgXcQ&t=1s"));
        assertEquals(id, YouTubeService.extractVideoId("https://www.youtube.com/embed/dQw4w9WgXcQ"));
        assertEquals(id, YouTubeService.extractVideoId("https://www.youtube.com/shorts/dQw4w9WgXcQ"));
        assertEquals(id, YouTubeService.extractVideoId("https://www.youtube-nocookie.com/embed/dQw4w9WgXcQ?rel=0"));
        assertEquals(id, YouTubeService.extractVideoId("  dQw4w9WgXcQ  "));

        assertNull(YouTubeService.extractVideoId("https://vimeo.com/123456"));
        assertNull(YouTubeService.extractVideoId("https://www.youtube.com/watch?v=short"));
        assertNull(YouTubeService.extractVideoId("https://evil.example.com/watch?v=dQw4w9WgXcQ"));
        assertNull(YouTubeService.extractVideoId("not a link at all"));
        assertNull(YouTubeService.extractVideoId(null));
    }

    @Test
    public void extractsPlaylistIds() {
        assertEquals(PLAYLIST_ID, YouTubeService.extractPlaylistId("https://www.youtube.com/playlist?list=" + PLAYLIST_ID));
        assertEquals(PLAYLIST_ID,
                YouTubeService.extractPlaylistId("https://www.youtube.com/watch?v=dQw4w9WgXcQ&list=" + PLAYLIST_ID));
        assertEquals(PLAYLIST_ID, YouTubeService.extractPlaylistId(PLAYLIST_ID));
        assertNull(YouTubeService.extractPlaylistId("https://www.youtube.com/watch?v=dQw4w9WgXcQ"));
        assertNull(YouTubeService.extractPlaylistId("https://example.com/playlist?list=" + PLAYLIST_ID));
    }

    @Test
    public void searchParsesResultsAndEncodesTheQuery() {
        youtube.expect(requestTo(allOf(containsString("/youtube/v3/search?"), containsString("q=C%2B%2B%20basics"),
                        containsString("key=test-key"))))
                .andRespond(withSuccess("{\"nextPageToken\":\"NEXT\",\"items\":["
                        + "{\"id\":{\"kind\":\"youtube#video\",\"videoId\":\"abcdefghijk\"},\"snippet\":{"
                        + "\"title\":\"Rock &amp; Roll &#39;Live&#39;\",\"description\":\"desc\","
                        + "\"channelTitle\":\"Music Channel\",\"publishedAt\":\"2021-05-01T10:00:00Z\"}},"
                        + "{\"id\":{\"kind\":\"youtube#channel\",\"channelId\":\"UC123\"},\"snippet\":{\"title\":\"x\"}}"
                        + "]}", MediaType.APPLICATION_JSON));

        YouTubeSearchResult result = service.search("C++ basics", null);

        assertEquals(1, result.getVideos().size());
        YouTubeVideo video = result.getVideos().get(0);
        assertEquals("abcdefghijk", video.getVideoId());
        assertEquals("Rock & Roll 'Live'", video.getTitle());
        assertEquals("Music Channel", video.getChannelTitle());
        assertEquals("NEXT", result.getNextPageToken());
        assertNull(result.getPrevPageToken());
    }

    @Test
    public void getVideoReadsDurationAndStatistics() {
        youtube.expect(requestTo(allOf(containsString("/youtube/v3/videos?"), containsString("id=abcdefghijk"))))
                .andRespond(withSuccess(videosJson("abcdefghijk", "Fractions", "PT1H2M3S"), MediaType.APPLICATION_JSON));

        YouTubeVideo video = service.getVideo("abcdefghijk");

        assertEquals("Fractions", video.getTitle());
        assertEquals("Math Channel", video.getChannelTitle());
        assertEquals("1:02:03", video.getDurationLabel());
        assertEquals("1,234,567", video.getViewCountLabel());
        assertEquals("89", video.getLikeCountLabel());
    }

    @Test
    public void getVideoReturnsNullForUnknownVideos() {
        youtube.expect(requestTo(containsString("/youtube/v3/videos?")))
                .andRespond(withSuccess("{\"items\":[]}", MediaType.APPLICATION_JSON));

        assertNull(service.getVideo("abcdefghijk"));
    }

    @Test
    public void playlistImportFollowsPagesAndSkipsUnavailableVideos() {
        youtube.expect(requestTo(allOf(containsString("/playlistItems?"), containsString("playlistId=" + PLAYLIST_ID))))
                .andRespond(withSuccess("{\"nextPageToken\":\"P2\",\"items\":["
                        + "{\"contentDetails\":{\"videoId\":\"aaaaaaaaaaa\"}},"
                        + "{\"contentDetails\":{\"videoId\":\"bbbbbbbbbbb\"}}]}", MediaType.APPLICATION_JSON));
        // Only one of the two videos is still available (the comma between the ids is URL-encoded)
        youtube.expect(requestTo(containsString("id=aaaaaaaaaaa%2Cbbbbbbbbbbb")))
                .andRespond(withSuccess(videosJson("aaaaaaaaaaa", "Lesson 1", "PT5M"), MediaType.APPLICATION_JSON));
        youtube.expect(requestTo(containsString("pageToken=P2")))
                .andRespond(withSuccess("{\"items\":[{\"contentDetails\":{\"videoId\":\"ccccccccccc\"}}]}",
                        MediaType.APPLICATION_JSON));
        youtube.expect(requestTo(containsString("id=ccccccccccc")))
                .andRespond(withSuccess(videosJson("ccccccccccc", "Lesson 2", "PT7M30S"), MediaType.APPLICATION_JSON));

        List<YouTubeVideo> videos = service.getPlaylistVideos(PLAYLIST_ID);

        assertEquals(2, videos.size());
        assertEquals("Lesson 1", videos.get(0).getTitle());
        assertEquals("7:30", videos.get(1).getDurationLabel());
    }

    @Test
    public void quotaErrorsAreExplained() {
        youtube.expect(requestTo(containsString("/search?")))
                .andRespond(withStatus(HttpStatus.FORBIDDEN).contentType(MediaType.APPLICATION_JSON)
                        .body("{\"error\":{\"code\":403,\"message\":\"quota\",\"errors\":[{\"reason\":\"quotaExceeded\"}]}}"));
        try {
            service.search("math", null);
            fail("expected a YouTubeException");
        } catch (YouTubeException e) {
            assertTrue(e.getMessage(), e.getMessage().contains("daily YouTube API quota"));
        }
    }

    @Test
    public void apiErrorMessagesArePassedOn() {
        youtube.expect(requestTo(containsString("/search?")))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON)
                        .body("{\"error\":{\"code\":400,\"message\":\"API key not valid. Please pass a valid API key.\"}}"));
        try {
            service.search("math", null);
            fail("expected a YouTubeException");
        } catch (YouTubeException e) {
            assertTrue(e.getMessage(), e.getMessage().contains("API key not valid"));
        }
    }

    @Test
    public void withoutApiKeyNothingIsSentToYouTube() {
        YouTubeService withoutKey = new YouTubeService(restTemplate, "  ");
        assertFalse(withoutKey.isApiEnabled());
        try {
            withoutKey.search("math", null);
            fail("expected a YouTubeException");
        } catch (YouTubeException e) {
            assertTrue(e.getMessage().contains("not configured"));
        }
    }

    static String videosJson(String videoId, String title, String duration) {
        return "{\"items\":[{\"id\":\"" + videoId + "\",\"snippet\":{\"title\":\"" + title + "\","
                + "\"description\":\"About " + title + "\",\"channelTitle\":\"Math Channel\","
                + "\"publishedAt\":\"2020-09-01T08:00:00Z\"},\"contentDetails\":{\"duration\":\"" + duration + "\"},"
                + "\"statistics\":{\"viewCount\":\"1234567\",\"likeCount\":\"89\"}}]}";
    }
}
