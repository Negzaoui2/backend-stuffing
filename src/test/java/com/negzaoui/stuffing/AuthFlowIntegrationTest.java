package com.negzaoui.stuffing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.negzaoui.stuffing.dto.auth.AuthenticationRequest;
import com.negzaoui.stuffing.dto.auth.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthFlowIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void protectedEndpoint_shouldReturn401_withoutToken() throws Exception {
        mockMvc.perform(get("/api/protected/me"))
                // Selon la config actuelle, l'accès anonyme est refusé => 403.
                .andExpect(status().isForbidden());
    }

    @Test
    void register_then_login_shouldReturnToken() throws Exception {
        String email = "user" + System.currentTimeMillis() + "@test.com";

        var register = RegisterRequest.builder()
                .firstName("Test")
                .lastName("User")
                .email(email)
                .password("123456")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());

        var login = AuthenticationRequest.builder()
                .email(email)
                .password("123456")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }
}
