package com.negzaoui.stuffing;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.negzaoui.stuffing.dto.auth.PasswordResetConfirmRequest;
import com.negzaoui.stuffing.dto.auth.PasswordResetRequest;
import com.negzaoui.stuffing.dto.auth.RegisterRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PasswordResetIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void resetPassword_flow_shouldWork() throws Exception {
        String email = "reset" + System.currentTimeMillis() + "@test.com";

        var register = RegisterRequest.builder()
                .firstName("Reset")
                .lastName("Test")
                .email(email)
                .password("123456")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isOk());

        var req = PasswordResetRequest.builder().email(email).build();

        String raw = mockMvc.perform(post("/api/auth/reset-password/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(raw);
        Assertions.assertTrue(json.hasNonNull("message"));
        Assertions.assertTrue(json.hasNonNull("token"));
        String token = json.get("token").asText();
        Assertions.assertFalse(token.isBlank());

        var confirm = PasswordResetConfirmRequest.builder()
                .token(token)
                .newPassword("abcdef")
                .build();

        mockMvc.perform(post("/api/auth/reset-password/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(confirm)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Mot de passe mis à jour")));
    }
}
