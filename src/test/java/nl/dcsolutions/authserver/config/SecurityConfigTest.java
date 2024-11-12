package nl.dcsolutions.authserver.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

class SecurityConfigTest {

    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        SecurityConfig securityConfig = new SecurityConfig();
        this.passwordEncoder = securityConfig.passwordEncoder();
    }

    @Test
    void passwordEncoderShouldBeDelegatingPasswordEncoder() {
        assertTrue(passwordEncoder instanceof DelegatingPasswordEncoder);
    }

    @Test
    void passwordEncoderShouldUseBcryptAsDefault() {
        String encodedPassword = passwordEncoder.encode("test-password");
        assertTrue(encodedPassword.startsWith("{bcrypt}"));
    }

}