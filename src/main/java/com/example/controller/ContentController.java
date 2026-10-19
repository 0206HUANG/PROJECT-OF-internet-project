package com.example.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.interceptor.AuthInterceptor;
import com.example.model.Classroom;
import com.example.model.Content;
import com.example.model.User;
import com.example.service.ActivityService;
import com.example.service.ClassService;
import com.example.service.ContentService;
import com.example.service.YouTubeService;
import com.example.service.YouTubeService.YouTubeException;

// Content library: uploads, viewing, editing and deleting
@Controller
public class ContentController {

    private final ContentService contentService;
    private final ClassService classService;
    private final ActivityService activityService;
    private final YouTubeService youTubeService;

    @Autowired
    public ContentController(ContentService contentService, ClassService classService,
                             ActivityService activityService, YouTubeService youTubeService) {
        this.contentService = contentService;
        this.classService = classService;
        this.activityService = activityService;
        this.youTubeService = youTubeService;
    }

    // Content upload page controller
    @GetMapping("/content/upload")
    public String showUploadContentPage() {
        return "uploadContent";
    }

    @PostMapping("/content/save")
    public String saveContent(@RequestParam("title") String title,
                              @RequestParam("description") String description,
                              @RequestParam(value = "file", required = false) MultipartFile file,
                              HttpSession session,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        String contentType = file == null ? null : ContentService.allowedContentType(file.getContentType());
        String error = null;
        if (!StringUtils.hasText(title) || !StringUtils.hasText(description)) {
            error = "Title and description are required.";
        } else if (file == null || file.isEmpty()) {
            error = "Please choose a file to upload.";
        } else if (contentType == null) {
            error = "Only image and video files can be uploaded.";
        }

        User user = AuthInterceptor.currentUser(session);
        if (error == null) {
            try {
                contentService.store(title.trim(), description.trim(), file, contentType, user);
            } catch (IOException e) {
                error = "The file could not be saved. Please try again.";
            }
        }
        if (error != null) {
            model.addAttribute("error", error);
            model.addAttribute("title", title);
            model.addAttribute("description", description);
            return "uploadContent";
        }

        activityService.record(user.getFullName() + " uploaded \"" + title.trim() + "\"");
        redirectAttributes.addFlashAttribute("message", "Content uploaded successfully!");
        return "redirect:/contentLibrary";
    }

    // Content library page controller, with search by text and filter by type
    @GetMapping("/contentLibrary")
    public String showContentLibrary(@RequestParam(value = "q", required = false) String query,
                                     @RequestParam(value = "type", required = false) String type,
                                     Model model) {
        model.addAttribute("contents", contentService.search(query, type));
        model.addAttribute("query", query);
        model.addAttribute("type", type);
        return "contentLibrary";
    }

    // Content detail page: plays the video (uploaded or YouTube) or shows the image
    @GetMapping("/content/view/{id}")
    public String showContent(@PathVariable("id") long id, HttpSession session, Model model) {
        Content content = findOr404(id);
        User user = AuthInterceptor.currentUser(session);
        model.addAttribute("content", content);
        model.addAttribute("canManage", canManage(user, content));
        model.addAttribute("inClasses", classService.findContaining(id));
        model.addAttribute("manageableClasses", classService.findManageableBy(user));
        if (content.isYoutube() && youTubeService.isApiEnabled()) {
            try {
                // Live numbers (views, likes) straight from YouTube
                model.addAttribute("liveVideo", youTubeService.getVideo(content.getYoutubeVideoId()));
            } catch (YouTubeException e) {
                // The video still plays; only the live statistics are missing
            }
        }
        return "contentView";
    }

    @PostMapping("/content/{id}/class")
    public String addToClass(@PathVariable("id") long id,
                             @RequestParam("classId") long classId,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        Content content = findOr404(id);
        Classroom classroom = classService.findById(classId);
        if (classroom == null || !classService.canManage(AuthInterceptor.currentUser(session), classroom)) {
            redirectAttributes.addFlashAttribute("error", "Please choose one of your classes.");
        } else if (classroom.addContent(id)) {
            redirectAttributes.addFlashAttribute("message", "\"" + content.getTitle() + "\" added to class \""
                    + classroom.getName() + "\".");
        } else {
            redirectAttributes.addFlashAttribute("message", "This item is already in class \""
                    + classroom.getName() + "\".");
        }
        return "redirect:/content/view/" + id;
    }

    // Streams an uploaded file so it can be viewed in the browser (supports seeking in videos)
    @GetMapping("/content/file/{id}")
    public ResponseEntity<Resource> showContentFile(@PathVariable("id") long id) {
        Content content = contentService.findById(id);
        if (content == null || content.isYoutube()) {
            return ResponseEntity.notFound().build();
        }
        Resource file = new FileSystemResource(contentService.getFile(content));
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }
        ContentDisposition disposition = ContentDisposition.builder("inline")
                .filename(content.getOriginalFilename(), StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(content.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .header("X-Content-Type-Options", "nosniff")
                .body(file);
    }

    // Edit page: only the person who added the item or an administrator may change it
    @GetMapping("/content/edit/{id}")
    public String showEditContentPage(@PathVariable("id") long id, HttpSession session, Model model,
                                      RedirectAttributes redirectAttributes) {
        Content content = findOr404(id);
        if (!canManage(AuthInterceptor.currentUser(session), content)) {
            return notAllowed(redirectAttributes, id);
        }
        model.addAttribute("content", content);
        model.addAttribute("title", content.getTitle());
        model.addAttribute("description", content.getDescription());
        return "editContent";
    }

    @PostMapping("/content/edit/{id}")
    public String updateContent(@PathVariable("id") long id,
                                @RequestParam("title") String title,
                                @RequestParam(value = "description", required = false) String description,
                                HttpSession session,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        Content content = findOr404(id);
        User user = AuthInterceptor.currentUser(session);
        if (!canManage(user, content)) {
            return notAllowed(redirectAttributes, id);
        }
        // Uploaded files always need a description; for YouTube videos it is optional
        boolean descriptionMissing = !content.isYoutube() && !StringUtils.hasText(description);
        if (!StringUtils.hasText(title) || descriptionMissing) {
            model.addAttribute("error", content.isYoutube() ? "Title is required." : "Title and description are required.");
            model.addAttribute("content", content);
            model.addAttribute("title", title);
            model.addAttribute("description", description);
            return "editContent";
        }
        contentService.update(id, title.trim(), description == null ? "" : description.trim());
        activityService.record(user.getFullName() + " edited \"" + title.trim() + "\"");
        redirectAttributes.addFlashAttribute("message", "Content updated successfully!");
        return "redirect:/content/view/" + id;
    }

    @PostMapping("/content/delete/{id}")
    public String deleteContent(@PathVariable("id") long id, HttpSession session,
                                RedirectAttributes redirectAttributes) {
        Content content = findOr404(id);
        User user = AuthInterceptor.currentUser(session);
        if (!canManage(user, content)) {
            return notAllowed(redirectAttributes, id);
        }
        contentService.delete(id);
        classService.removeContentEverywhere(id);
        activityService.record(user.getFullName() + " deleted \"" + content.getTitle() + "\"");
        redirectAttributes.addFlashAttribute("message", "Content deleted.");
        return "redirect:/contentLibrary";
    }

    private Content findOr404(long id) {
        Content content = contentService.findById(id);
        if (content == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return content;
    }

    private static boolean canManage(User user, Content content) {
        return user.isAdmin() || content.getUploadedBy().equals(user.getUsername());
    }

    private static String notAllowed(RedirectAttributes redirectAttributes, long id) {
        redirectAttributes.addFlashAttribute("error", "Only the person who added this item or an administrator can change it.");
        return "redirect:/content/view/" + id;
    }
}
