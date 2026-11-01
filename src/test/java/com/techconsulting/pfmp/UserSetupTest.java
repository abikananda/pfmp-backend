package com.techconsulting.pfmp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techconsulting.pfmp.model.JwtResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.springframework.test.util.AssertionErrors.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = "/script-before.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Transactional
public class UserSetupTest {

    @Autowired
    private MockMvc mockMvc;
    static String accessToken;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testUserSetup() throws Exception {
        doRegister();
        accessToken = doLogin();
        assertNotNull("No accessToken returned", accessToken);
    }
    void doRegister() throws Exception {
        mockMvc.perform(post("/api/auth/signUp")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(DataUtility.getSignUpForm())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User registered successfully!"));
    }
    String doLogin() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/signIn")
                        .header("Authorization", "Basic "+ Base64.getEncoder().encodeToString((DataUtility.getUsername()+":"+DataUtility.getPassword()).getBytes(StandardCharsets.UTF_8))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(DataUtility.getUsername())).andReturn();
        String responseContent = result.getResponse().getContentAsString();
        JwtResponse jwtResponse = objectMapper.readValue(responseContent, JwtResponse.class);
        return jwtResponse.getAccessToken();
    }
}
