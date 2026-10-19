package com.example.controller;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
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
import com.example.model.Role;
import com.example.model.User;
import com.example.service.ActivityService;
import com.example.service.ClassService;
import com.example.service.ContentService;
import com.example.service.SchoolService;
import com.example.service.UserService;
import com.example.service.YouTubeService;

// Login, registration, dashboards and reports
@Controller
public class EduStreamController {

    private final UserService userService;
    private final SchoolService schoolService;
    private final ContentService contentService;
    private final ClassService classService;
    private final ActivityService activityService;
    private final YouTubeService youTubeService;

    @Autowired
    public EduStreamController(UserService userService, SchoolService schoolService, ContentService contentService,
                               ClassService classService, ActivityService activityService,
                               YouTubeService youTubeService) {
        this.userService = userService;
        this.schoolService = schoolService;
        this.contentService = contentService;
        this.classService = classService;
        this.activityService = activityService;
        this.youTubeService = youTubeService;
    }

    // Home page: logged-in users go to their dashboard, everyone else to the login page
    @GetMapping("/")
    public String home(HttpSession session) {
        User user = AuthInterceptor.currentUser(session);
        return user == null ? "redirect:/login" : dashboardRedirect(user);
    }

    // Login page controller
    @GetMapping("/login")
    public String showLoginPage(HttpSession session) {
        User user = AuthInterceptor.currentUser(session);
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
        model.addAttribute("totalClasses", classService.count());
        model.addAttribute("activities", activityService.recent());
        model.addAttribute("youtubeEnabled", youTubeService.isApiEnabled());
        return "adminDashboard";
    }

    // Teacher dashboard controller
    @GetMapping("/teacherDashboard")
    public String showTeacherDashboard(HttpSession session, Model model) {
        User user = AuthInterceptor.currentUser(session);
        model.addAttribute("myClasses", classService.findByTeacher(user.getUsername()));
        model.addAttribute("myContents", contentService.findByUploader(user.getUsername()));
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
        model.addAttribute("youtubeContents", contentService.countYoutube());
        model.addAttribute("totalClasses", classService.count());
        return "reports";
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
