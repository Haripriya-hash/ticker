package com.rts.ticker;

import com.rts.ticker.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ChangePasswordIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @WithMockUser(username = "pwd-user")
    void loggedInUserCanChangeTheirPassword() throws Exception {
        // Seed the user directly with a known hashed password
        var user = new com.rts.ticker.model.User("pwd-user", passwordEncoder.encode("oldpassword"));
        userRepository.save(user);

        mockMvc.perform(post("/change-password")
                        .with(csrf())
                        .param("currentPassword", "oldpassword")
                        .param("newPassword", "newpassword123")
                        .param("confirmPassword", "newpassword123"))
                .andExpect(status().isOk())
                .andExpect(view().name("change-password"))
                .andExpect(model().attributeExists("successMessage"));

        var updatedUser = userRepository.findByUsername("pwd-user").orElseThrow();
        assertThat(passwordEncoder.matches("newpassword123", updatedUser.getPassword())).isTrue();
        assertThat(passwordEncoder.matches("oldpassword", updatedUser.getPassword())).isFalse();
    }

    @Test
    @WithMockUser(username = "wrong-pwd-user")
    void rejectsChangeWhenCurrentPasswordIsWrong() throws Exception {
        var user = new com.rts.ticker.model.User("wrong-pwd-user", passwordEncoder.encode("correctpassword"));
        userRepository.save(user);

        mockMvc.perform(post("/change-password")
                        .with(csrf())
                        .param("currentPassword", "wrongguess")
                        .param("newPassword", "newpassword123")
                        .param("confirmPassword", "newpassword123"))
                .andExpect(status().isOk())
                .andExpect(view().name("change-password"))
                .andExpect(model().attributeExists("errorMessage"));

        var unchangedUser = userRepository.findByUsername("wrong-pwd-user").orElseThrow();
        assertThat(passwordEncoder.matches("correctpassword", unchangedUser.getPassword())).isTrue();
    }

    @Test
    @WithMockUser(username = "mismatch-user")
    void rejectsChangeWhenConfirmationDoesNotMatch() throws Exception {
        var user = new com.rts.ticker.model.User("mismatch-user", passwordEncoder.encode("originalpass"));
        userRepository.save(user);

        mockMvc.perform(post("/change-password")
                        .with(csrf())
                        .param("currentPassword", "originalpass")
                        .param("newPassword", "newpassword123")
                        .param("confirmPassword", "somethingelse"))
                .andExpect(status().isOk())
                .andExpect(view().name("change-password"))
                .andExpect(model().attributeExists("errorMessage"));
    }

    @Test
    void anonymousUserCannotReachChangePassword() throws Exception {
        mockMvc.perform(get("/change-password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }
}
