package com.example.controller;

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
import com.example.model.School;
import com.example.service.ActivityService;
import com.example.service.SchoolService;

// School management (administrators only, see the interceptor configuration)
@Controller
public class SchoolController {

    private final SchoolService schoolService;
    private final ActivityService activityService;

    @Autowired
    public SchoolController(SchoolService schoolService, ActivityService activityService) {
        this.schoolService = schoolService;
        this.activityService = activityService;
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
        String error = validate(name, location, status);
        if (error != null) {
            return showFormWithError(model, error, null, name, location, status);
        }
        schoolService.add(name.trim(), location.trim(), status);
        activityService.record(AuthInterceptor.currentUser(session).getFullName()
                + " added school \"" + name.trim() + "\"");
        redirectAttributes.addFlashAttribute("message", "School added successfully!");
        return "redirect:/schools";
    }

    // Edit school page controller (reuses the add school form)
    @GetMapping("/schools/edit/{id}")
    public String showEditSchoolPage(@PathVariable("id") long id, Model model,
                                     RedirectAttributes redirectAttributes) {
        School school = schoolService.findById(id);
        if (school == null) {
            redirectAttributes.addFlashAttribute("error", "That school no longer exists.");
            return "redirect:/schools";
        }
        model.addAttribute("schoolId", school.getId());
        model.addAttribute("name", school.getName());
        model.addAttribute("location", school.getLocation());
        model.addAttribute("status", school.getStatus());
        return "addSchool";
    }

    @PostMapping("/schools/update/{id}")
    public String updateSchool(@PathVariable("id") long id,
                               @RequestParam("name") String name,
                               @RequestParam("location") String location,
                               @RequestParam("status") String status,
                               HttpSession session,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        String error = validate(name, location, status);
        if (error != null) {
            return showFormWithError(model, error, id, name, location, status);
        }
        if (schoolService.update(id, name.trim(), location.trim(), status) == null) {
            redirectAttributes.addFlashAttribute("error", "That school no longer exists.");
            return "redirect:/schools";
        }
        activityService.record(AuthInterceptor.currentUser(session).getFullName()
                + " updated school \"" + name.trim() + "\"");
        redirectAttributes.addFlashAttribute("message", "School updated successfully!");
        return "redirect:/schools";
    }

    @PostMapping("/schools/delete/{id}")
    public String deleteSchool(@PathVariable("id") long id, HttpSession session,
                               RedirectAttributes redirectAttributes) {
        School school = schoolService.delete(id);
        if (school != null) {
            activityService.record(AuthInterceptor.currentUser(session).getFullName()
                    + " deleted school \"" + school.getName() + "\"");
            redirectAttributes.addFlashAttribute("message", "School deleted.");
        }
        return "redirect:/schools";
    }

    private static String validate(String name, String location, String status) {
        if (!StringUtils.hasText(name) || !StringUtils.hasText(location)) {
            return "School name and location are required.";
        }
        if (!"Active".equals(status) && !"Inactive".equals(status)) {
            return "Please choose a valid status.";
        }
        return null;
    }

    private static String showFormWithError(Model model, String error, Long schoolId, String name,
                                            String location, String status) {
        model.addAttribute("error", error);
        model.addAttribute("schoolId", schoolId);
        model.addAttribute("name", name);
        model.addAttribute("location", location);
        model.addAttribute("status", status);
        return "addSchool";
    }
}
