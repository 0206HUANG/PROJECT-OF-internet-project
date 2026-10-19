package com.example.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.interceptor.AuthInterceptor;
import com.example.model.Content;
import com.example.model.Role;
import com.example.model.User;
import com.example.service.ActivityService;
import com.example.service.ContentService;
import com.example.service.SchoolService;
import com.example.service.UserService;

@Controller
public class EduStreamController {

    private final UserService userService;
    private final SchoolService schoolService;
    private final ContentService contentService;
    private final ActivityService activityService;

    @Autowired
    public EduStreamController(UserService userService, SchoolService schoolService,
                               ContentService contentService, ActivityService activityService) {
        this.userService = userService;
        this.schoolService = schoolService;
        this.contentService = contentService;
        this.activityService = activityService;
    }

    // Home page: logged-in users go to their dashboard, everyone else to the login page
    @GetMapping("/")
    public String home(HttpSession session) {
        User user = currentUser(session);
        return user == null ? "redirect:/login" : dashboardRedirect(user);
    }

    // Login page controller
    @GetMapping("/login")
    public String showLoginPage(HttpSession session) {
        User user = currentUser(session);
        return user == null ? "login" : dashboardRedirect(user);
    }

    @PostMapping("/login")
    public String loginUser(@RequestParam("username") String username,
                            @RequestParam("password") String password,
                            HttpServletRequest request,
                            Model model) {
        User user = userService.authenticate(username, password);
        if (user == null) {
            model.addAttribute("error", "Incorrect username or password");
            model.addAttribute("username", username);
            return "login";
        }
        request.getSession().setAttribute(AuthInterceptor.SESSION_USER, user);
        // Issue a new session id after login to prevent session fixation
        request.changeSessionId();
        return dashboardRedirect(user);
    }

    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        session.invalidate();
        redirectAttributes.addFlashAttribute("message", "You have been logged out.");
        return "redirect:/login";
    }

    // Register page controller
    @GetMapping("/register")
    public String showRegisterPage() {
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@RequestParam("fullName") String fullName,
                               @RequestParam("email") String email,
                               @RequestParam("role") String role,
                               @RequestParam("password") String password,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        Role selectedRole = parseRole(role);
        String error = null;
        if (!StringUtils.hasText(fullName) || !StringUtils.hasText(email) || !StringUtils.hasText(password)) {
            error = "Please fill in all fields.";
        } else if (!email.contains("@")) {
            error = "Please enter a valid email address.";
        } else if (selectedRole == null) {
            error = "Please choose a valid role.";
        } else if (!userService.register(fullName.trim(), email, password, selectedRole)) {
            error = "An account with this email already exists.";
        }

        if (error != null) {
            model.addAttribute("error", error);
            model.addAttribute("fullName", fullName);
            model.addAttribute("email", email);
            model.addAttribute("role", role);
            return "register";
        }
        activityService.record("New " + selectedRole.name().toLowerCase(Locale.ROOT) + " account registered: "
                + fullName.trim());
        redirectAttributes.addFlashAttribute("message", "Registration successful! Please log in.");
        return "redirect:/login";
    }

    // Admin dashboard controller
    @GetMapping("/adminDashboard")
    public String showAdminDashboard(Model model) {
        model.addAttribute("totalSchools", schoolService.count());
        model.addAttribute("totalUsers", userService.count());
        model.addAttribute("totalContents", contentService.count());
        model.addAttribute("activities", activityService.recent());
        return "adminDashboard";
    }

    // Teacher dashboard controller
    @GetMapping("/teacherDashboard")
    public String showTeacherDashboard(HttpSession session, Model model) {
        // Classes are still sample data: the application has no class management yet
        model.addAttribute("myClasses", new String[]{"Math Class", "English Class"});
        model.addAttribute("myContents", contentService.findByUploader(currentUser(session).getUsername()));
        return "teacherDashboard";
    }

    // Reports page controller
    @GetMapping("/reports")
    public String showReports(Model model) {
        int totalSchools = schoolService.count();
        int activeSchools = schoolService.countActive();
        model.addAttribute("totalSchools", totalSchools);
        model.addAttribute("activeSchools", activeSchools);
        model.addAttribute("inactiveSchools", totalSchools - activeSchools);
        model.addAttribute("totalUsers", userService.count());
        model.addAttribute("adminCount", userService.countByRole(Role.ADMIN));
        model.addAttribute("teacherCount", userService.countByRole(Role.TEACHER));
        model.addAttribute("totalContents", contentService.count());
        return "reports";
    }

    // School list page controller
    @GetMapping("/schools")
    public String showSchoolList(Model model) {
        model.addAttribute("schools", schoolService.findAll());
        return "schoolList";
    }

    // Add school page controller
    @GetMapping("/schools/add")
    public String showAddSchoolPage() {
        return "addSchool";
    }

    @PostMapping("/schools/save")
    public String saveSchool(@RequestParam("name") String name,
                             @RequestParam("location") String location,
                             @RequestParam("status") String status,
                             HttpSession session,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        String error = null;
        if (!StringUtils.hasText(name) || !StringUtils.hasText(location)) {
            error = "School name and location are required.";
        } else if (!"Active".equals(status) && !"Inactive".equals(status)) {
            error = "Please choose a valid status.";
        }

        if (error != null) {
            model.addAttribute("error", error);
            model.addAttribute("name", name);
            model.addAttribute("location", location);
            model.addAttribute("status", status);
            return "addSchool";
        }

        schoolService.add(name.trim(), location.trim(), status);
        activityService.record(currentUser(session).getFullName() + " added school \"" + name.trim() + "\"");
        redirectAttributes.addFlashAttribute("message", "School added successfully!");
        return "redirect:/schools";
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

        User user = currentUser(session);
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

    // Content library page controller
    @GetMapping("/contentLibrary")
    public String showContentLibrary(Model model) {
        model.addAttribute("contents", contentService.findAll());
        return "contentLibrary";
    }

    // Streams an uploaded file so it can be viewed in the browser (supports seeking in videos)
    @GetMapping("/content/file/{id}")
    public ResponseEntity<Resource> showContentFile(@PathVariable("id") long id) {
        Content content = contentService.findById(id);
        if (content == null) {
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

    private static User currentUser(HttpSession session) {
        return (User) session.getAttribute(AuthInterceptor.SESSION_USER);
    }

    private static String dashboardRedirect(User user) {
        return user.isAdmin() ? "redirect:/adminDashboard" : "redirect:/teacherDashboard";
    }

    private static Role parseRole(String role) {
        try {
            return Role.valueOf(role);
        } catch (IllegalArgumentException | NullPointerException e) {
            return null;
        }
    }
}
