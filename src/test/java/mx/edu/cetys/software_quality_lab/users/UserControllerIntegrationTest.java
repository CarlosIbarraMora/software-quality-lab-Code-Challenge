package mx.edu.cetys.software_quality_lab.users;

import mx.edu.cetys.software_quality_lab.validators.EmailValidatorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailValidatorService emailValidatorService;

    // Limpiar la BD antes de cada prueba para garantizar un estado independiente
    @BeforeEach
    public void limpiarBD() {
        userRepository.deleteAll();
    }

    // ─── POST /users ──────────────────────────────────────────────────────────
    @Test
    void shouldCreateUserAndReturn201() throws Exception {
        String body = """
            {
                "username": "juan4_dev",
                "firstName": "Juan",
                "lastName": "Pérez",
                "phone": "6641234567",
                "email": "str4nger#tst.vld",
                "age": 25
            }""";

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.info").value("New user created"))
                .andExpect(jsonPath("$.response").exists())
                .andExpect(jsonPath("$.response.user").exists())
                .andExpect(jsonPath("$.response.user.username").value("juan4_dev"))
                .andExpect(jsonPath("$.response.user.firstName").value("Juan"))
                .andExpect(jsonPath("$.response.user.lastName").value("Pérez"))
                .andExpect(jsonPath("$.response.user.phone").value("6641234567"))
                .andExpect(jsonPath("$.response.user.email").value("str4nger#tst.vld"))
                .andExpect(jsonPath("$.response.user.age").value(25))
                .andExpect(jsonPath("$.response.user.status").value("ACTIVE"));
    }

    @Test
    void shouldReturn400WhenUsernameIsTooShort() throws Exception {
        String body = """
            {
                "username": "juan",
                "firstName": "Juan",
                "lastName": "Pérez",
                "phone": "6641234567",
                "email": "juan4#gmail.com",
                "age": 25
            }""";

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.info").exists());
    }

    @Test
    void shouldReturn400WhenAgeIsExactlyTwelve() throws Exception {
        String body = """
            {
                "username": "juan4_dev",
                "firstName": "Juan",
                "lastName": "Pérez",
                "phone": "6641234567",
                "email": "juan4#gmail.com",
                "age": 12
            }""";

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.info").exists());
    }

    @Test
    void shouldReturn400WhenPhoneIsInvalid() throws Exception {
        String body = """
            {
                "username": "juan4_dev",
                "firstName": "Juan",
                "lastName": "Pérez",
                "phone": "123",
                "email": "juan4#gmail.com",
                "age": 25
            }""";

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.info").exists());
    }

    @Test
    void shouldReturn400WhenEmailIsInvalid() throws Exception {
        String body = """
            {
                "username": "juan4_dev",
                "firstName": "Juan",
                "lastName": "Pérez",
                "phone": "6641234567",
                "email": "user@gmail.com",
                "age": 25
            }""";

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.info").exists());
    }

    @Test
    void shouldReturn409WhenUsernameIsDuplicated() throws Exception {
        userRepository.save(
                new User("juan4_dev", "Juan", "Perez", "6641234567", "juan4#gmail.com", 25)
        );

        String body = """
            {
                "username": "juan4_dev",
                "firstName": "Juan",
                "lastName": "Pérez",
                "phone": "6641234567",
                "email": "str4nger#tst.vld",
                "age": 25
            }""";

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.info").exists());
    }

    @Test
    void shouldReturn200AndUserWhenFound() throws Exception {
        var user = userRepository.save(
                new User("juan4_dev", "Juan", "Perez", "6641234567", "juan4#gmail.com", 25)
        );

        mockMvc.perform(get("/users/{id}", user.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").exists())
                .andExpect(jsonPath("$.response.user").exists())
                .andExpect(jsonPath("$.response.user.username").value("juan4_dev"))
                .andExpect(jsonPath("$.response.user.firstName").value("Juan"))
                .andExpect(jsonPath("$.response.user.lastName").value("Perez"))
                .andExpect(jsonPath("$.response.user.phone").value("6641234567"))
                .andExpect(jsonPath("$.response.user.email").value("juan4#gmail.com"))
                .andExpect(jsonPath("$.response.user.age").value(25))
                .andExpect(jsonPath("$.response.user.status").value("ACTIVE"));
    }

    @Test
    void shouldReturn404WhenUserNotFound() throws Exception {
        mockMvc.perform(get("/users/9999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.info").exists());
    }

    // ─── PATCH /users/{id}/suspend ────────────────────────────────────────────

    @Test
    void shouldSuspendUserAndReturn200() throws Exception {
        // TODO: guardar un usuario ACTIVE via repository
        // TODO: realizar PATCH /users/{id}/suspend
        // TODO: andExpect status 200
        // TODO: andExpect jsonPath("$.response.user.status") == "SUSPENDED"
    }

    @Test
    void shouldReturn400WhenSuspendingAlreadySuspendedUser() throws Exception {
        // TODO: guardar un usuario con status SUSPENDED via repository
        // TODO: realizar PATCH /users/{id}/suspend
        // TODO: andExpect status 400
    }
}
