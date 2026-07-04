package com.rts.ticker;

import com.rts.ticker.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void signupCreatesANewUserWithAHashedPassword() throws Exception {
        mockMvc.perform(post("/signup")
                        .with(csrf())
                        .param("username", "priya")
                        .param("password", "correct-horse"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?signupSuccess"));

        var savedUser = userRepository.findByUsername("priya").orElseThrow();
        assertThat(savedUser.getPassword()).isNotEqualTo("correct-horse");
        assertThat(passwordEncoder.matches("correct-horse", savedUser.getPassword())).isTrue();
    }

    @Test
    void signupRejectsADuplicateUsername() throws Exception {
        mockMvc.perform(post("/signup")
                .with(csrf())
                .param("username", "duplicate-user")
                .param("password", "password1"));

        mockMvc.perform(post("/signup")
                        .with(csrf())
                        .param("username", "duplicate-user")
                        .param("password", "password2"))
                .andExpect(status().isOk())
                .andExpect(view().name("signup"))
                .andExpect(model().attributeExists("errorMessage"));
    }

    @Test
    void aRegisteredUserCanLogInAndReachTheDashboard() throws Exception {
        mockMvc.perform(post("/signup")
                .with(csrf())
                .param("username", "login-user")
                .param("password", "mypassword"));

        mockMvc.perform(SecurityMockMvcRequestBuilders.formLogin("/login")
                        .user("login-user")
                        .password("mypassword"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));
    }

    @Test
    void anonymousUsersCannotReachTheDashboard() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }
}
