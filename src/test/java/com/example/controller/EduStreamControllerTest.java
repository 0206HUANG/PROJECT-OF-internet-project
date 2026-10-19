package com.example.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.List;

import org.junit.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;

import com.example.model.Content;
import com.example.model.School;

public class EduStreamControllerTest extends AbstractWebTest {

    @Test
    public void anonymousUsersAreSentToLogin() throws Exception {
        mockMvc.perform(get("/")).andExpect(redirectedUrl("/login"));
        mockMvc.perform(get("/adminDashboard")).andExpect(redirectedUrl("/login"));
        mockMvc.perform(get("/contentLibrary")).andExpect(redirectedUrl("/login"));
        mockMvc.perform(get("/login")).andExpect(status().isOk()).andExpect(view().name("login"));
    }

    @Test
    public void adminLoginOpensAdminDashboard() throws Exception {
        MockHttpSession session = login("admin", "admin", "/adminDashboard");

        mockMvc.perform(get("/adminDashboard").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("adminDashboard"))
                .andExpect(model().attributeExists("totalSchools", "totalUsers", "totalContents", "activities"));
        mockMvc.perform(get("/reports").session(session)).andExpect(view().name("reports"));
        mockMvc.perform(get("/").session(session)).andExpect(redirectedUrl("/adminDashboard"));
    }

    @Test
    public void wrongPasswordShowsError() throws Exception {
        mockMvc.perform(post("/login").param("username", "admin").param("password", "wrong"))
                .andExpect(view().name("login"))
                .andExpect(model().attribute("error", "Incorrect username or password"));
    }

    @Test
    public void teachersCannotOpenAdminPages() throws Exception {
        MockHttpSession session = login("teacher", "teacher", "/teacherDashboard");

        mockMvc.perform(get("/teacherDashboard").session(session)).andExpect(view().name("teacherDashboard"));
        mockMvc.perform(get("/adminDashboard").session(session)).andExpect(redirectedUrl("/teacherDashboard"));
        mockMvc.perform(get("/adminDashboard/").session(session)).andExpect(redirectedUrl("/teacherDashboard"));
        mockMvc.perform(get("/schools").session(session)).andExpect(redirectedUrl("/teacherDashboard"));
        mockMvc.perform(post("/schools/save").session(session)
                        .param("name", "Sneaky School").param("location", "X").param("status", "Active"))
                .andExpect(redirectedUrl("/teacherDashboard"));
        mockMvc.perform(get("/reports").session(session)).andExpect(redirectedUrl("/teacherDashboard"));
    }

    @Test
    public void registeredUserCanLogInWithEmail() throws Exception {
        mockMvc.perform(post("/register").param("fullName", "Alice Chen").param("email", "Alice@Example.com")
                        .param("role", "TEACHER").param("password", "secret"))
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attribute("message", "Registration successful! Please log in."));

        mockMvc.perform(post("/register").param("fullName", "Alice Again").param("email", "alice@example.com")
                        .param("role", "TEACHER").param("password", "other"))
                .andExpect(view().name("register"))
                .andExpect(model().attribute("error", "An account with this email already exists."));

        login("alice@example.com", "secret", "/teacherDashboard");
    }

    @Test
    public void registrationRejectsInvalidRole() throws Exception {
        mockMvc.perform(post("/register").param("fullName", "Bob").param("email", "bob@example.com")
                        .param("role", "SUPERUSER").param("password", "secret"))
                .andExpect(view().name("register"))
                .andExpect(model().attribute("error", "Please choose a valid role."));
    }

    @Test
    public void savedSchoolAppearsInList() throws Exception {
        MockHttpSession session = login("admin", "admin", "/adminDashboard");

        mockMvc.perform(post("/schools/save").session(session)
                        .param("name", "Riverside High").param("location", "West District").param("status", "Active"))
                .andExpect(redirectedUrl("/schools"))
                .andExpect(flash().attribute("message", "School added successfully!"));

        School school = findSchool(session, "Riverside High");
        assertNotNull("saved school should be listed", school);
        assertEquals("West District", school.getLocation());
    }

    @Test
    public void uploadedImageIsListedAndViewable() throws Exception {
        MockHttpSession session = login("teacher", "teacher", "/teacherDashboard");
        byte[] bytes = {(byte) 0x89, 'P', 'N', 'G'};

        mockMvc.perform(multipart("/content/save").file(new MockMultipartFile("file", "photo.png", "image/png", bytes))
                        .param("title", "Lesson Photo").param("description", "Whiteboard notes").session(session))
                .andExpect(redirectedUrl("/contentLibrary"));

        List<Content> contents = modelAttribute(mockMvc.perform(get("/contentLibrary").session(session)).andReturn(),
                "contents");
        Content uploaded = null;
        for (Content item : contents) {
            if ("Lesson Photo".equals(item.getTitle())) {
                uploaded = item;
            }
        }
        assertNotNull("uploaded content should be listed", uploaded);
        assertEquals("photo.png", uploaded.getOriginalFilename());

        mockMvc.perform(get("/content/file/" + uploaded.getId()).session(session))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/png"))
                .andExpect(content().bytes(bytes));
        mockMvc.perform(get("/content/file/999999").session(session)).andExpect(status().isNotFound());
    }

    @Test
    public void uploadRejectsNonMediaFiles() throws Exception {
        MockHttpSession session = login("teacher", "teacher", "/teacherDashboard");

        mockMvc.perform(multipart("/content/save")
                        .file(new MockMultipartFile("file", "page.html", "text/html", "<script>".getBytes("UTF-8")))
                        .param("title", "Bad").param("description", "Not media").session(session))
                .andExpect(view().name("uploadContent"))
                .andExpect(model().attribute("error", "Only image and video files can be uploaded."));
    }

    @Test
    public void logoutEndsTheSession() throws Exception {
        MockHttpSession session = login("admin", "admin", "/adminDashboard");

        mockMvc.perform(get("/logout").session(session))
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attribute("message", "You have been logged out."));
        assertTrue(session.isInvalid());
    }

    @Test
    public void schoolsCanBeEditedAndDeleted() throws Exception {
        MockHttpSession session = login("admin", "admin", "/adminDashboard");
        mockMvc.perform(post("/schools/save").session(session)
                .param("name", "Old Name").param("location", "Old Town").param("status", "Active"));
        long id = findSchool(session, "Old Name").getId();

        mockMvc.perform(get("/schools/edit/" + id).session(session))
                .andExpect(view().name("addSchool"))
                .andExpect(model().attribute("name", "Old Name"));
        mockMvc.perform(post("/schools/update/" + id).session(session)
                        .param("name", "New Name").param("location", "New Town").param("status", "Inactive"))
                .andExpect(redirectedUrl("/schools"));
        School updated = findSchool(session, "New Name");
        assertEquals("New Town", updated.getLocation());
        assertEquals("Inactive", updated.getStatus());

        mockMvc.perform(post("/schools/delete/" + id).session(session)).andExpect(redirectedUrl("/schools"));
        assertNull(findSchool(session, "New Name"));
    }

    @Test
    public void teachersManageTheirClasses() throws Exception {
        MockHttpSession teacher = login("teacher", "teacher", "/teacherDashboard");
        Content photo = upload(teacher, "Class Photo");

        MvcResult created = mockMvc.perform(post("/classes/save").session(teacher)
                        .param("name", "Science 101").param("description", "Plants").param("schoolName", "School A"))
                .andReturn();
        String classUrl = created.getResponse().getRedirectedUrl();
        assertTrue(classUrl.startsWith("/classes/"));

        mockMvc.perform(post(classUrl + "/content").session(teacher).param("contentId", String.valueOf(photo.getId())))
                .andExpect(redirectedUrl(classUrl));
        MvcResult page = mockMvc.perform(get(classUrl).session(teacher)).andExpect(view().name("classDetail")).andReturn();
        List<Content> contents = modelAttribute(page, "contents");
        assertEquals(1, contents.size());
        assertNull("no YouTube videos, so no playlist player", modelAttribute(page, "playAllUrl"));

        // Another teacher cannot open or change this class
        mockMvc.perform(post("/register").param("fullName", "Other Teacher").param("email", "other@example.com")
                .param("role", "TEACHER").param("password", "secret"));
        MockHttpSession other = login("other@example.com", "secret", "/teacherDashboard");
        mockMvc.perform(get(classUrl).session(other)).andExpect(redirectedUrl("/classes"));
        mockMvc.perform(post(classUrl + "/delete").session(other)).andExpect(redirectedUrl("/classes"));

        mockMvc.perform(post(classUrl + "/content/" + photo.getId() + "/remove").session(teacher))
                .andExpect(redirectedUrl(classUrl));
        mockMvc.perform(post(classUrl + "/delete").session(teacher)).andExpect(redirectedUrl("/classes"));
        mockMvc.perform(get(classUrl).session(teacher)).andExpect(redirectedUrl("/classes"));
    }

    @Test
    public void onlyTheOwnerOrAnAdminCanEditOrDeleteContent() throws Exception {
        MockHttpSession teacher = login("teacher", "teacher", "/teacherDashboard");
        Content content = upload(teacher, "Owned Upload");
        mockMvc.perform(post("/register").param("fullName", "Third Teacher").param("email", "third@example.com")
                .param("role", "TEACHER").param("password", "secret"));
        MockHttpSession other = login("third@example.com", "secret", "/teacherDashboard");

        mockMvc.perform(post("/content/edit/" + content.getId()).session(other)
                        .param("title", "Hacked").param("description", "x"))
                .andExpect(flash().attributeExists("error"));
        mockMvc.perform(post("/content/edit/" + content.getId()).session(teacher)
                        .param("title", "Renamed Upload").param("description", "New description"))
                .andExpect(redirectedUrl("/content/view/" + content.getId()));
        mockMvc.perform(get("/content/view/" + content.getId()).session(teacher))
                .andExpect(view().name("contentView"))
                .andExpect(model().attribute("canManage", true));

        MockHttpSession admin = login("admin", "admin", "/adminDashboard");
        mockMvc.perform(post("/content/delete/" + content.getId()).session(admin))
                .andExpect(redirectedUrl("/contentLibrary"));
        mockMvc.perform(get("/content/view/" + content.getId()).session(admin)).andExpect(status().isNotFound());
    }

    @Test
    public void librarySearchFiltersByTextAndType() throws Exception {
        MockHttpSession teacher = login("teacher", "teacher", "/teacherDashboard");
        upload(teacher, "Volcano Diagram");

        MvcResult byText = mockMvc.perform(get("/contentLibrary").session(teacher).param("q", "volcano")).andReturn();
        List<Content> found = modelAttribute(byText, "contents");
        assertEquals(1, found.size());

        MvcResult byType = mockMvc.perform(get("/contentLibrary").session(teacher)
                .param("q", "volcano").param("type", "youtube")).andReturn();
        List<Content> none = modelAttribute(byType, "contents");
        assertTrue(none.isEmpty());
    }

    private Content upload(MockHttpSession session, String title) throws Exception {
        mockMvc.perform(multipart("/content/save")
                        .file(new MockMultipartFile("file", "image.png", "image/png", new byte[]{1, 2, 3}))
                        .param("title", title).param("description", "Test upload").session(session))
                .andExpect(redirectedUrl("/contentLibrary"));
        List<Content> contents = modelAttribute(
                mockMvc.perform(get("/contentLibrary").session(session).param("q", title)).andReturn(), "contents");
        return contents.get(0);
    }

    private School findSchool(MockHttpSession session, String name) throws Exception {
        List<School> schools = modelAttribute(mockMvc.perform(get("/schools").session(session)).andReturn(), "schools");
        for (School school : schools) {
            if (school.getName().equals(name)) {
                return school;
            }
        }
        return null;
    }
}
