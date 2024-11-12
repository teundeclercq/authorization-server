package nl.dcsolutions.authserver.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.dcsolutions.authserver.config.SecurityConfig;
import nl.dcsolutions.authserver.domain.Client;
import nl.dcsolutions.authserver.service.JpaRegisteredClientRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({ClientRegistrationController.class, SecurityConfig.class})
@AutoConfigureMockMvc
class ClientRegistrationControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JpaRegisteredClientRepository jpaRegisteredClientRepository;

    @MockBean
    private JwtDecoder jwtDecoder;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    void accessProtectedEndpointWithoutScopeShouldFail() throws Exception {

        // Test payload
        Client clientDTO = new Client();
        clientDTO.setClientId("test-client-id");
        clientDTO.setClientName("Test Client");
        clientDTO.setClientSecret("test-secret");

        mockMvc.perform(post("/oauth2/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clientDTO)))
                .andExpect(status().isForbidden());  // Should return 403 if no scope
    }

    @Test
    @WithMockUser(authorities = {"SCOPE_client:register"})
    void accessProtectedEndpointWithScopeShouldSucceed() throws Exception {

        // Test payload
        Client client = new Client();
        client.setClientId("test-client-id");
        client.setClientName("Test Client");
        client.setClientSecret("test-secret");


        mockMvc.perform(post("/oauth2/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(client)))
                .andExpect(status().isOk());  // Should return 200 if correct scope is present
    }

    @Test
    void accessOtherEndpointWithoutAuthenticationShouldFail() throws Exception {
        mockMvc.perform(get("/some-other-protected-endpoint"))
                .andExpect(status().isUnauthorized());  // Should return 401 if not authenticated
    }
}