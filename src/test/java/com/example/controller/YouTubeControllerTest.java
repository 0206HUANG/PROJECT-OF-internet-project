package com.example.controller;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.net.URI;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.client.RestTemplate;

import com.example.model.Classroom;
import com.example.model.Content;
import com.example.model.YouTubeSearchResult;
import com.example.model.YouTubeVideo;
import com.example.service.ClassService;
import com.example.service.ContentService;
import com.example.service.UserService;
import com.example.service.YouTubeService;

public class YouTubeControllerTest extends AbstractWebTest {

    @Autowired
    private YouTubeService youTubeService;
    @Autowired
    private ContentService contentService;
    @Autowired
    private ClassService classService;
    @Autowired
    private UserService userService;

    // Stands in for the YouTube Data API
    private MockRestServiceServer youtube;

    @Before
    public void mockYouTubeApi() {
        RestTemplate restTemplate = (RestTemplate) ReflectionTestUtils.getField(youTubeService, "restTemplate");
        youtube = MockRestServiceServer.bindTo(restTemplate).build();
    }

    @After
    public void verifyYouTubeCalls() {
        youtube.verify();
    }

    @Test
    public void addingByLinkFillsInTheDetailsFromYouTube() throws Exception {
        MockHttpSession session = login("teacher", "teacher", "/teacherDashboard");
        expectVideo("aaaaaaaaaa1", "Intro to Fractions", "PT4M5S");

        MvcResult result = mockMvc.perform(post("/youtube/add").session(session)
                        .param("url", "https://youtu.be/aaaaaaaaaa1").param("title", ""))
                .andExpect(flash().attribute("message", "\"Intro to Fractions\" added to the library."))
                .andReturn();

        Content content = contentService.findByYoutubeVideoId("aaaaaaaaaa1");
        assertEquals("/content/view/" + content.getId(), result.getResponse().getRedirectedUrl());
        assertEquals("Intro to Fractions", content.getTitle());
        assertEquals("About Intro to Fractions", content.getDescription());
        assertEquals("Math Channel", content.getYoutubeVideo().getChannelTitle());
        assertEquals("4:05", content.getYoutubeVideo().getDurationLabel());
    }

    @Test
    public void addingAnExistingVideoToAClassReusesTheLibraryItem() throws Exception {
        MockHttpSession session = login("teacher", "teacher", "/teacherDashboard");
        Classroom mathClass = classService.findByTeacher("teacher").get(0);
        expectVideo("bbbbbbbbbb2", "Long Division", "PT10M");
        expectVideo("bbbbbbbbbb2", "Long Division", "PT10M");

        mockMvc.perform(post("/youtube/add").session(session).param("url", "bbbbbbbbbb2"));
        mockMvc.perform(post("/youtube/add").session(session).param("url", "https://www.youtube.com/watch?v=bbbbbbbbbb2")
                        .param("classId", String.valueOf(mathClass.getId())))
                .andExpect(redirectedUrl("/classes/" + mathClass.getId()))
                .andExpect(flash().attribute("message",
                        "\"Long Division\" is already in the library; it is now in class \"" + mathClass.getName() + "\"."));

        Content content = contentService.findByYoutubeVideoId("bbbbbbbbbb2");
        assertTrue(classService.findById(mathClass.getId()).getContentIds().contains(content.getId()));
        assertEquals(1, contentService.search("Long Division", "youtube").size());
    }

    @Test
    public void invalidLinksAreRejected() throws Exception {
        MockHttpSession session = login("teacher", "teacher", "/teacherDashboard");

        mockMvc.perform(post("/youtube/add").session(session).param("url", "https://vimeo.com/123").param("title", "x"))
                .andExpect(view().name("youtube"))
                .andExpect(model().attribute("error",
                        "Please enter a valid YouTube video link, e.g. https://www.youtube.com/watch?v=..."));
    }

    @Test
    public void typedTitleIsUsedWhenYouTubeIsUnreachable() throws Exception {
        MockHttpSession session = login("teacher", "teacher", "/teacherDashboard");
        youtube.expect(once(), requestTo(containsString("/videos?"))).andRespond(withServerError());

        mockMvc.perform(post("/youtube/add").session(session)
                .param("url", "https://www.youtube.com/watch?v=ccccccccc_3").param("title", "Typed Title"));

        assertEquals("Typed Title", contentService.findByYoutubeVideoId("ccccccccc_3").getTitle());
    }

    @Test
    public void viewPageShowsLiveStatistics() throws Exception {
        MockHttpSession session = login("teacher", "teacher", "/teacherDashboard");
        Content content = contentService.addYoutubeVideo("Live Stats", "",
                YouTubeVideo.basic("ddddddddd-4", "Live Stats", ""), userService.findByUsername("teacher"));
        expectVideo("ddddddddd-4", "Live Stats", "PT1M");

        MvcResult result = mockMvc.perform(get("/content/view/" + content.getId()).session(session))
                .andExpect(view().name("contentView"))
                .andReturn();
        YouTubeVideo live = modelAttribute(result, "liveVideo");
        assertEquals("1,234,567", live.getViewCountLabel());
    }

    @Test
    public void searchShowsResultsAndAddingReturnsToThem() throws Exception {
        MockHttpSession session = login("teacher", "teacher", "/teacherDashboard");
        String searchResponse = "{\"nextPageToken\":\"NEXT\",\"items\":[{\"id\":{\"videoId\":\"eeeeeeeeee5\"},"
                + "\"snippet\":{\"title\":\"Fractions Song\",\"channelTitle\":\"Kids Math\"}}]}";
        youtube.expect(requestTo(allOf(containsString("/search?"), containsString("q=fractions%20song"))))
                .andRespond(withSuccess(searchResponse, MediaType.APPLICATION_JSON));
        expectVideo("eeeeeeeeee5", "Fractions Song", "PT2M");
        youtube.expect(requestTo(containsString("/search?")))
                .andRespond(withSuccess(searchResponse, MediaType.APPLICATION_JSON));

        MvcResult page = mockMvc.perform(get("/youtube").session(session).param("q", "fractions song"))
                .andExpect(view().name("youtube"))
                .andExpect(model().attribute("apiEnabled", true))
                .andReturn();
        YouTubeSearchResult result = modelAttribute(page, "result");
        assertEquals("Fractions Song", result.getVideos().get(0).getTitle());
        assertEquals("NEXT", result.getNextPageToken());

        String redirect = mockMvc.perform(post("/youtube/add").session(session).param("url", "eeeeeeeeee5")
                        .param("title", "Fractions Song").param("q", "fractions song").param("pageToken", "NEXT"))
                .andExpect(redirectedUrl("/youtube?q=fractions+song&pageToken=NEXT"))
                .andReturn().getResponse().getRedirectedUrl();
        assertNotNull(contentService.findByYoutubeVideoId("eeeeeeeeee5"));

        // Following the redirect (as a browser does) shows the same results page with the success message
        mockMvc.perform(get(URI.create(redirect)).session(session))
                .andExpect(model().attribute("message", "\"Fractions Song\" added to the library."));
    }

    @Test
    public void searchErrorsAreShownOnThePage() throws Exception {
        MockHttpSession session = login("teacher", "teacher", "/teacherDashboard");
        youtube.expect(requestTo(containsString("/search?")))
                .andRespond(withStatus(HttpStatus.FORBIDDEN).contentType(MediaType.APPLICATION_JSON)
                        .body("{\"error\":{\"errors\":[{\"reason\":\"quotaExceeded\"}]}}"));

        mockMvc.perform(get("/youtube").session(session).param("q", "anything"))
                .andExpect(model().attribute("error",
                        "YouTube request failed: the daily YouTube API quota has been used up. Please try again tomorrow."));
    }

    @Test
    public void playlistImportAddsEveryVideoToTheClass() throws Exception {
        MockHttpSession session = login("teacher", "teacher", "/teacherDashboard");
        Classroom englishClass = classService.findByTeacher("teacher").get(1);
        youtube.expect(requestTo(containsString("/playlistItems?")))
                .andRespond(withSuccess("{\"items\":[{\"contentDetails\":{\"videoId\":\"fffffffff_6\"}},"
                        + "{\"contentDetails\":{\"videoId\":\"ggggggggg_7\"}}]}", MediaType.APPLICATION_JSON));
        youtube.expect(requestTo(containsString("id=fffffffff_6%2Cggggggggg_7")))
                .andRespond(withSuccess("{\"items\":["
                        + "{\"id\":\"fffffffff_6\",\"snippet\":{\"title\":\"Grammar 1\"},\"contentDetails\":{\"duration\":\"PT3M\"}},"
                        + "{\"id\":\"ggggggggg_7\",\"snippet\":{\"title\":\"Grammar 2\"},\"contentDetails\":{\"duration\":\"PT4M\"}}"
                        + "]}", MediaType.APPLICATION_JSON));

        mockMvc.perform(post("/youtube/import").session(session)
                        .param("playlistUrl", "https://www.youtube.com/playlist?list=PLx0sYbCqOb8TBPRdmBHs5Iftvv9TPboYG")
                        .param("classId", String.valueOf(englishClass.getId())))
                .andExpect(redirectedUrl("/classes/" + englishClass.getId()))
                .andExpect(flash().attribute("message",
                        "Imported 2 new video(s) from the playlist into class \"" + englishClass.getName() + "\"."));

        Classroom updated = classService.findById(englishClass.getId());
        assertTrue(updated.getContentIds().contains(contentService.findByYoutubeVideoId("fffffffff_6").getId()));
        assertTrue(updated.getContentIds().contains(contentService.findByYoutubeVideoId("ggggggggg_7").getId()));

        // The class page now offers a player that plays its YouTube videos one after another
        MvcResult classPage = mockMvc.perform(get("/classes/" + englishClass.getId()).session(session)).andReturn();
        String playAllUrl = modelAttribute(classPage, "playAllUrl");
        assertTrue(playAllUrl, playAllUrl.startsWith("https://www.youtube-nocookie.com/embed/"));
        assertTrue(playAllUrl, playAllUrl.contains("?playlist="));
    }

    @Test
    public void teachersCannotAddVideosToOtherTeachersClasses() throws Exception {
        mockMvc.perform(post("/register").param("fullName", "Fourth Teacher").param("email", "fourth@example.com")
                .param("role", "TEACHER").param("password", "secret"));
        MockHttpSession other = login("fourth@example.com", "secret", "/teacherDashboard");
        Classroom notTheirs = classService.findByTeacher("teacher").get(0);

        mockMvc.perform(post("/youtube/add").session(other).param("url", "hhhhhhhhh_8").param("title", "Sneaky")
                        .param("classId", String.valueOf(notTheirs.getId())))
                .andExpect(view().name("youtube"))
                .andExpect(model().attribute("error", "Please choose one of your classes."));
    }

    private void expectVideo(String videoId, String title, String duration) {
        youtube.expect(once(), requestTo(allOf(containsString("/videos?"), containsString("id=" + videoId))))
                .andRespond(withSuccess("{\"items\":[{\"id\":\"" + videoId + "\",\"snippet\":{\"title\":\"" + title
                        + "\",\"description\":\"About " + title + "\",\"channelTitle\":\"Math Channel\"},"
                        + "\"contentDetails\":{\"duration\":\"" + duration + "\"},"
                        + "\"statistics\":{\"viewCount\":\"1234567\",\"likeCount\":\"89\"}}]}", MediaType.APPLICATION_JSON));
    }
}
