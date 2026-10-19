package com.example.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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

import java.nio.file.Files;
import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.example.model.Content;
import com.example.model.School;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration("src/main/webapp")
@ContextConfiguration("file:src/main/webapp/WEB-INF/myFrontController-servlet.xml")
public class EduStreamControllerTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeClass
    public static void useTemporaryUploadDir() throws Exception {
        System.setProperty("edustream.upload.dir", Files.createTempDirectory("edustream-test").toString());
    }

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

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

        MvcResult result = mockMvc.perform(get("/schools").session(session)).andReturn();
        @SuppressWarnings("unchecked")
        List<School> schools = (List<School>) result.getModelAndView().getModel().get("schools");
        boolean found = false;
        for (School school : schools) {
            found |= "Riverside High".equals(school.getName()) && "West District".equals(school.getLocation());
        }
        assertTrue("saved school should be listed", found);
    }

    @Test
    public void uploadedImageIsListedAndViewable() throws Exception {
        MockHttpSession session = login("teacher", "teacher", "/teacherDashboard");
        byte[] bytes = {(byte) 0x89, 'P', 'N', 'G'};

        mockMvc.perform(multipart("/content/save").file(new MockMultipartFile("file", "photo.png", "image/png", bytes))
                        .param("title", "Lesson Photo").param("description", "Whiteboard notes").session(session))
                .andExpect(redirectedUrl("/contentLibrary"));

        MvcResult result = mockMvc.perform(get("/contentLibrary").session(session)).andReturn();
        @SuppressWarnings("unchecked")
        List<Content> contents = (List<Content>) result.getModelAndView().getModel().get("contents");
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

    private MockHttpSession login(String username, String password, String expectedRedirect) throws Exception {
        MvcResult result = mockMvc.perform(post("/login").param("username", username).param("password", password))
                .andExpect(redirectedUrl(expectedRedirect))
                .andReturn();
        return (MockHttpSession) result.getRequest().getSession(false);
    }
}
