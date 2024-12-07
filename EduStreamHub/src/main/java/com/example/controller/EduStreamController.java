package com.example.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/")
public class EduStreamController {

    // Login page controller
    @GetMapping("login")
    public String showLoginPage() {
        // Return the login.jsp page
        return "login";
    }

    @PostMapping("login")
    public String loginUser(@RequestParam("username") String username, 
                            @RequestParam("password") String password, 
                            Model model) {
        // Simple logic: If both username and password are "admin", login succeeds
        if ("admin".equals(username) && "admin".equals(password)) {
            return "redirect:/adminDashboard";
        } else {
            model.addAttribute("error", "Incorrect username or password");
            return "login";
        }
    }

    // Register page controller
    @GetMapping("register")
    public String showRegisterPage() {
        // Return the register.jsp page
        return "register";
    }

    @PostMapping("register")
    public String registerUser(@RequestParam("fullName") String fullName, 
                               @RequestParam("email") String email, 
                               @RequestParam("role") String role, 
                               @RequestParam("password") String password, 
                               Model model) {
        // Add user registration logic here, such as saving user information to the database
        model.addAttribute("message", "Registration successful! Please log in.");
        return "login";
    }

    // Admin dashboard controller
    @GetMapping("adminDashboard")
    public String showAdminDashboard(Model model) {
        // Mock some data to display on the admin dashboard
        model.addAttribute("totalSchools", 10);
        model.addAttribute("activeUsers", 50);
        model.addAttribute("pendingApprovals", 5);
        return "adminDashboard";
    }

    // Teacher dashboard controller
    @GetMapping("teacherDashboard")
    public String showTeacherDashboard(Model model) {
        // Mock teacher's class list
        model.addAttribute("myClasses", new String[]{"Math Class", "English Class"});
        return "teacherDashboard";
    }

    // School list page controller
    @GetMapping("schools")
    public String showSchoolList(Model model) {
        // Mock school list data
        model.addAttribute("schools", new String[]{"School A", "School B", "School C"});
        return "schoolList";
    }

    // Add school page controller
    @GetMapping("schools/add")
    public String showAddSchoolPage() {
        return "addSchool";
    }

    @PostMapping("schools/save")
    public String saveSchool(@RequestParam("name") String name, 
                             @RequestParam("location") String location, 
                             @RequestParam("status") String status, 
                             Model model) {
        // Logic to save the school, such as adding it to the database
        model.addAttribute("message", "School added successfully!");
        return "redirect:/schools";
    }

    // Content upload page controller
    @GetMapping("content/upload")
    public String showUploadContentPage() {
        return "uploadContent";
    }

    @PostMapping("content/save")
    public String saveContent(@RequestParam("title") String title, 
                              @RequestParam("description") String description, 
                              @RequestParam("file") String file) {
        // Logic to save the content, such as storing file information
        return "redirect:/contentLibrary";
    }

    // Content library page controller
    @GetMapping("contentLibrary")
    public String showContentLibrary(Model model) {
        // Mock some content data
        model.addAttribute("contents", new String[]{"Content 1", "Content 2", "Content 3"});
        return "contentLibrary";
    }
}
