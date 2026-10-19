package com.example.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

import java.nio.file.Files;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

// Loads the real Spring MVC configuration (controllers, interceptors, multipart resolver)
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration("src/main/webapp")
@ContextConfiguration("file:src/main/webapp/WEB-INF/myFrontController-servlet.xml")
public abstract class AbstractWebTest {

    @Autowired
    protected WebApplicationContext context;

    protected MockMvc mockMvc;

    @BeforeClass
    public static void configureSystemProperties() throws Exception {
        // The Spring context is shared by all test classes, so every class must use the same settings
        if (System.getProperty("edustream.upload.dir") == null) {
            System.setProperty("edustream.upload.dir", Files.createTempDirectory("edustream-test").toString());
        }
        System.setProperty("youtube.api.key", "test-key");
    }

    @Before
    public void setUpMockMvc() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    protected MockHttpSession login(String username, String password, String expectedRedirect) throws Exception {
        MvcResult result = mockMvc.perform(post("/login").param("username", username).param("password", password))
                .andExpect(redirectedUrl(expectedRedirect))
                .andReturn();
        return (MockHttpSession) result.getRequest().getSession(false);
    }

    @SuppressWarnings("unchecked")
    protected static <T> T modelAttribute(MvcResult result, String name) {
        return (T) result.getModelAndView().getModel().get(name);
    }
}
