package com.example.controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.interceptor.AuthInterceptor;
import com.example.model.Classroom;
import com.example.model.Content;
import com.example.model.School;
import com.example.model.User;
import com.example.model.YouTubeVideo;
import com.example.service.ActivityService;
import com.example.service.ClassService;
import com.example.service.ContentService;
import com.example.service.SchoolService;

// Classes: teachers group library content (uploads and YouTube videos) into a playlist per class
@Controller
public class ClassController {

    private final ClassService classService;
    private final ContentService contentService;
    private final SchoolService schoolService;
    private final ActivityService activityService;

    @Autowired
    public ClassController(ClassService classService, ContentService contentService, SchoolService schoolService,
                           ActivityService activityService) {
        this.classService = classService;
        this.contentService = contentService;
        this.schoolService = schoolService;
        this.activityService = activityService;
    }

    // Class list and "create class" form
    @GetMapping("/classes")
    public String showClasses(HttpSession session, Model model) {
        model.addAttribute("classes", classService.findManageableBy(AuthInterceptor.currentUser(session)));
        model.addAttribute("schools", schoolService.findAll());
        return "classes";
    }

    @PostMapping("/classes/save")
    public String saveClass(@RequestParam("name") String name,
                            @RequestParam(value = "description", required = false) String description,
                            @RequestParam(value = "schoolName", required = false) String schoolName,
                            HttpSession session,
                            Model model,
                            RedirectAttributes redirectAttributes) {
        User user = AuthInterceptor.currentUser(session);
        String school = schoolName == null ? "" : schoolName.trim();
        String error = null;
        if (!StringUtils.hasText(name)) {
            error = "Class name is required.";
        } else if (!school.isEmpty() && !schoolExists(school)) {
            error = "Please choose a school from the list.";
        }
        if (error != null) {
            model.addAttribute("error", error);
            model.addAttribute("classes", classService.findManageableBy(user));
            model.addAttribute("schools", schoolService.findAll());
            model.addAttribute("name", name);
            model.addAttribute("description", description);
            model.addAttribute("schoolName", schoolName);
            return "classes";
        }

        Classroom classroom = classService.create(name.trim(), description == null ? "" : description.trim(),
                school, user);
        activityService.record(user.getFullName() + " created class \"" + classroom.getName() + "\"");
        redirectAttributes.addFlashAttribute("message", "Class created! Now add some content to it.");
        return "redirect:/classes/" + classroom.getId();
    }

    // Class page: its content, a YouTube "play all" player and forms to manage the content
    @GetMapping("/classes/{id}")
    public String showClass(@PathVariable("id") long id, HttpSession session, Model model,
                            RedirectAttributes redirectAttributes) {
        Classroom classroom = classService.findById(id);
        if (classroom == null || !classService.canManage(AuthInterceptor.currentUser(session), classroom)) {
            redirectAttributes.addFlashAttribute("error", "That class does not exist or is not yours.");
            return "redirect:/classes";
        }
        List<Content> contents = contentService.findByIds(classroom.getContentIds());
        List<Content> available = new ArrayList<>();
        for (Content content : contentService.findAll()) {
            if (!classroom.getContentIds().contains(content.getId())) {
                available.add(content);
            }
        }
        model.addAttribute("classroom", classroom);
        model.addAttribute("contents", contents);
        model.addAttribute("availableContents", available);
        model.addAttribute("playAllUrl", playAllUrl(contents));
        return "classDetail";
    }

    @PostMapping("/classes/{id}/content")
    public String addContentToClass(@PathVariable("id") long id,
                                    @RequestParam("contentId") long contentId,
                                    HttpSession session,
                                    RedirectAttributes redirectAttributes) {
        Classroom classroom = classService.findById(id);
        Content content = contentService.findById(contentId);
        if (classroom == null || !classService.canManage(AuthInterceptor.currentUser(session), classroom)) {
            redirectAttributes.addFlashAttribute("error", "That class does not exist or is not yours.");
            return "redirect:/classes";
        }
        if (content == null) {
            redirectAttributes.addFlashAttribute("error", "That content no longer exists.");
        } else if (classroom.addContent(contentId)) {
            redirectAttributes.addFlashAttribute("message", "\"" + content.getTitle() + "\" added to the class.");
        } else {
            redirectAttributes.addFlashAttribute("message", "\"" + content.getTitle() + "\" is already in this class.");
        }
        return "redirect:/classes/" + id;
    }

    @PostMapping("/classes/{id}/content/{contentId}/remove")
    public String removeContentFromClass(@PathVariable("id") long id,
                                         @PathVariable("contentId") long contentId,
                                         HttpSession session,
                                         RedirectAttributes redirectAttributes) {
        Classroom classroom = classService.findById(id);
        if (classroom == null || !classService.canManage(AuthInterceptor.currentUser(session), classroom)) {
            redirectAttributes.addFlashAttribute("error", "That class does not exist or is not yours.");
            return "redirect:/classes";
        }
        classroom.removeContent(contentId);
        redirectAttributes.addFlashAttribute("message", "Removed from the class.");
        return "redirect:/classes/" + id;
    }

    @PostMapping("/classes/{id}/delete")
    public String deleteClass(@PathVariable("id") long id, HttpSession session,
                              RedirectAttributes redirectAttributes) {
        User user = AuthInterceptor.currentUser(session);
        Classroom classroom = classService.findById(id);
        if (classroom == null || !classService.canManage(user, classroom)) {
            redirectAttributes.addFlashAttribute("error", "That class does not exist or is not yours.");
            return "redirect:/classes";
        }
        classService.delete(id);
        activityService.record(user.getFullName() + " deleted class \"" + classroom.getName() + "\"");
        redirectAttributes.addFlashAttribute("message", "Class deleted. Its content is still in the library.");
        return "redirect:/classes";
    }

    private boolean schoolExists(String name) {
        for (School school : schoolService.findAll()) {
            if (school.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    // Embed URL that plays every YouTube video of the class one after another, or null if there are none
    private static String playAllUrl(List<Content> contents) {
        List<String> videoIds = new ArrayList<>();
        for (Content content : contents) {
            if (content.isYoutube()) {
                videoIds.add(content.getYoutubeVideoId());
            }
        }
        if (videoIds.isEmpty()) {
            return null;
        }
        String url = YouTubeVideo.embedUrl(videoIds.get(0));
        if (videoIds.size() > 1) {
            url += "?playlist=" + String.join(",", videoIds.subList(1, videoIds.size()));
        }
        return url;
    }
}
