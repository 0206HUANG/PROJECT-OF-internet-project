package com.example.controller;

import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.interceptor.AuthInterceptor;
import com.example.model.Classroom;
import com.example.model.Content;
import com.example.model.User;
import com.example.model.YouTubeVideo;
import com.example.service.ActivityService;
import com.example.service.ClassService;
import com.example.service.ContentService;
import com.example.service.YouTubeService;
import com.example.service.YouTubeService.YouTubeException;

// YouTube integration: search YouTube, add videos by link and import whole playlists
@Controller
public class YouTubeController {

    private final YouTubeService youTubeService;
    private final ContentService contentService;
    private final ClassService classService;
    private final ActivityService activityService;

    @Autowired
    public YouTubeController(YouTubeService youTubeService, ContentService contentService,
                             ClassService classService, ActivityService activityService) {
        this.youTubeService = youTubeService;
        this.contentService = contentService;
        this.classService = classService;
        this.activityService = activityService;
    }

    // YouTube page: search with result pages, "add by link" and "import playlist" forms
    @GetMapping("/youtube")
    public String showYouTubePage(@RequestParam(value = "q", required = false) String query,
                                  @RequestParam(value = "pageToken", required = false) String pageToken,
                                  @RequestParam(value = "classId", required = false) Long classId,
                                  HttpSession session,
                                  Model model) {
        addPageAttributes(model, AuthInterceptor.currentUser(session), classId);
        if (StringUtils.hasText(query)) {
            model.addAttribute("query", query.trim());
            model.addAttribute("pageToken", pageToken);
            if (youTubeService.isApiEnabled()) {
                try {
                    model.addAttribute("result", youTubeService.search(query.trim(), pageToken));
                } catch (YouTubeException e) {
                    model.addAttribute("error", e.getMessage());
                }
            }
        }
        return "youtube";
    }

    /**
     * Adds one video to the library (or reuses it if it is already there) and optionally to a class.
     * With an API key, the title, description, channel and duration are filled in from YouTube.
     */
    @PostMapping("/youtube/add")
    public String addYouTubeVideo(@RequestParam("url") String url,
                                  @RequestParam(value = "title", required = false) String title,
                                  @RequestParam(value = "description", required = false) String description,
                                  @RequestParam(value = "classId", required = false) Long classId,
                                  @RequestParam(value = "q", required = false) String searchQuery,
                                  @RequestParam(value = "pageToken", required = false) String pageToken,
                                  HttpSession session,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        User user = AuthInterceptor.currentUser(session);
        String videoId = YouTubeService.extractVideoId(url);
        Classroom classroom = classId == null ? null : classService.findById(classId);
        String error = null;
        YouTubeVideo details = null;

        if (videoId == null) {
            error = "Please enter a valid YouTube video link, e.g. https://www.youtube.com/watch?v=...";
        } else if (classId != null && (classroom == null || !classService.canManage(user, classroom))) {
            error = "Please choose one of your classes.";
        } else if (youTubeService.isApiEnabled()) {
            try {
                details = youTubeService.getVideo(videoId);
                if (details == null) {
                    error = "This YouTube video does not exist or is private.";
                }
            } catch (YouTubeException e) {
                // YouTube is unreachable: fall back to the title typed by the user
            }
        }
        String finalTitle = StringUtils.hasText(title) ? title.trim() : details == null ? null : details.getTitle();
        if (error == null && !StringUtils.hasText(finalTitle)) {
            error = "Please enter a title for the video.";
        }

        if (error != null) {
            if (StringUtils.hasText(searchQuery)) {
                redirectAttributes.addFlashAttribute("error", error);
                return redirectToSearch(redirectAttributes, searchQuery, pageToken, classId);
            }
            addPageAttributes(model, user, classId);
            model.addAttribute("error", error);
            model.addAttribute("url", url);
            model.addAttribute("title", title);
            model.addAttribute("description", description);
            return "youtube";
        }

        String finalDescription = StringUtils.hasText(description) ? description.trim()
                : details == null ? "" : details.getDescription();
        Content content = contentService.findByYoutubeVideoId(videoId);
        boolean alreadyInLibrary = content != null;
        if (!alreadyInLibrary) {
            YouTubeVideo video = details != null ? details : YouTubeVideo.basic(videoId, finalTitle, finalDescription);
            content = contentService.addYoutubeVideo(finalTitle, finalDescription, video, user);
            activityService.record(user.getFullName() + " added YouTube video \"" + finalTitle + "\"");
        }
        if (classroom != null) {
            classroom.addContent(content.getId());
        }
        redirectAttributes.addFlashAttribute("message", addedMessage(content, alreadyInLibrary, classroom));

        if (StringUtils.hasText(searchQuery)) {
            return redirectToSearch(redirectAttributes, searchQuery, pageToken, classId);
        }
        return classroom != null ? "redirect:/classes/" + classroom.getId() : "redirect:/content/view/" + content.getId();
    }

    // Imports every public video of a YouTube playlist into the library (and optionally a class)
    @PostMapping("/youtube/import")
    public String importPlaylist(@RequestParam("playlistUrl") String playlistUrl,
                                 @RequestParam(value = "classId", required = false) Long classId,
                                 HttpSession session,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        User user = AuthInterceptor.currentUser(session);
        String playlistId = YouTubeService.extractPlaylistId(playlistUrl);
        Classroom classroom = classId == null ? null : classService.findById(classId);
        String error = null;
        List<YouTubeVideo> videos = null;

        if (!youTubeService.isApiEnabled()) {
            error = "Importing playlists needs a YouTube API key (see the README).";
        } else if (playlistId == null) {
            error = "Please enter a valid YouTube playlist link, e.g. https://www.youtube.com/playlist?list=...";
        } else if (classId != null && (classroom == null || !classService.canManage(user, classroom))) {
            error = "Please choose one of your classes.";
        } else {
            try {
                videos = youTubeService.getPlaylistVideos(playlistId);
                if (videos.isEmpty()) {
                    error = "No public videos were found in this playlist.";
                }
            } catch (YouTubeException e) {
                error = e.getMessage();
            }
        }
        if (error != null) {
            addPageAttributes(model, user, classId);
            model.addAttribute("error", error);
            model.addAttribute("playlistUrl", playlistUrl);
            return "youtube";
        }

        int added = 0;
        for (YouTubeVideo video : videos) {
            Content content = contentService.findByYoutubeVideoId(video.getVideoId());
            if (content == null) {
                content = contentService.addYoutubeVideo(video.getTitle(), video.getDescription(), video, user);
                added++;
            }
            if (classroom != null) {
                classroom.addContent(content.getId());
            }
        }
        activityService.record(user.getFullName() + " imported " + added + " videos from a YouTube playlist");
        String message = "Imported " + added + " new video(s) from the playlist";
        if (added < videos.size()) {
            message += " (" + (videos.size() - added) + " already in the library)";
        }
        if (classroom != null) {
            message += " into class \"" + classroom.getName() + "\"";
        }
        redirectAttributes.addFlashAttribute("message", message + ".");
        return classroom != null ? "redirect:/classes/" + classroom.getId() : "redirect:/contentLibrary?type=youtube";
    }

    private void addPageAttributes(Model model, User user, Long classId) {
        model.addAttribute("apiEnabled", youTubeService.isApiEnabled());
        model.addAttribute("classes", classService.findManageableBy(user));
        model.addAttribute("classId", classId);
    }

    // Back to the same page of search results
    private static String redirectToSearch(RedirectAttributes redirectAttributes, String query, String pageToken,
                                           Long classId) {
        // Added to the redirect URL as (encoded) query parameters
        redirectAttributes.addAttribute("q", query);
        if (StringUtils.hasText(pageToken)) {
            redirectAttributes.addAttribute("pageToken", pageToken);
        }
        if (classId != null) {
            redirectAttributes.addAttribute("classId", classId);
        }
        return "redirect:/youtube";
    }

    private static String addedMessage(Content content, boolean alreadyInLibrary, Classroom classroom) {
        String message = alreadyInLibrary
                ? "\"" + content.getTitle() + "\" is already in the library"
                : "\"" + content.getTitle() + "\" added to the library";
        if (classroom != null) {
            message += (alreadyInLibrary ? "; it is now in class \"" : " and to class \"") + classroom.getName() + "\"";
        }
        return message + ".";
    }
}
