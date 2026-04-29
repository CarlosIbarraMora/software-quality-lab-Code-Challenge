package mx.edu.cetys.software_quality_lab.users;

import jdk.jshell.Snippet;
import mx.edu.cetys.software_quality_lab.pets.Pet;
import mx.edu.cetys.software_quality_lab.users.exceptions.DuplicateUsernameException;
import mx.edu.cetys.software_quality_lab.users.exceptions.InvalidUserDataException;
import mx.edu.cetys.software_quality_lab.users.exceptions.UserNotFoundException;
import mx.edu.cetys.software_quality_lab.validators.EmailValidatorService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    UserRepository userRepository;

    // EmailValidatorService debe ser mockeado — en pruebas unitarias no probamos dependencias externas
    @Mock
    EmailValidatorService emailValidatorService;

    @InjectMocks
    UserService userService;

    // ─── Caso exitoso ─────────────────────────────────────────────────────────

    UserController.UserRequest request = new UserController.UserRequest("andypro", "Andres", "Silva", "6682359422", "Andy#b.c", 14);
    private User buildMockSavedUser(Long id, String username, String firstName, String lastName, String phone, String email, Integer age, UserStatus status) {
        User user = new User(username, firstName, lastName, phone, email, age);
        user.setId(id);
        user.setStatus(status);
        return user;
    }

    @Test
    void shouldRegisterUserSuccessfully() {
        // arrange — construir un UserRequest válido, mockear emailValidatorService.isValid para que regrese true,
        var mockedUser = buildMockSavedUser(
                1L,
                request.username(),
                request.firstName(),
                request.lastName(),
                request.phone(),
                request.email(),
                request.age(),
                UserStatus.ACTIVE);

        when(userRepository.existsByUsername(request.username())).thenReturn(false);
        when(userRepository.save(any())).thenReturn(mockedUser);
        // act — llamar a userService.registerUser(request)
        var response = userService.registerUser(request);
        // assert — verificar id, username, status == "ACTIVE"; confirmar que save fue llamado una vez
        verify(userRepository, times(1)).save(any());
        assertEquals(1L, response.id());
        assertEquals("andypro", response.username());
        assertEquals("Andres", response.firstName());
        assertEquals("Silva", response.lastName());
        assertEquals("6682359422", response.phone());
        assertEquals("Andy#b.c", response.email());
        assertEquals(14 , response.age());
        assertEquals(UserStatus.ACTIVE.toString(), response.status());
    }

    @Test
    void shouldGetUserByIdSuccessfully() {
        // arrange — mockear userRepository.findById para que regrese un Optional<User> con datos
        var mockedUser = buildMockSavedUser(
                1L,
                request.username(),
                request.firstName(),
                request.lastName(),
                request.phone(),
                request.email(),
                request.age(),
                UserStatus.ACTIVE);

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockedUser));

        // act — llamar a userService.getUserById(1L)
        var response = userService.getUserById(1L);

        // assert — verificar que los campos response coincidan con el mock
        verify(userRepository, times(1)).findById(1L);
        assertEquals(1L, response.id());
        assertEquals("andypro", response.username());
        assertEquals("Andres", response.firstName());
        assertEquals("Silva", response.lastName());
        assertEquals("6682359422", response.phone());
        assertEquals("Andy#b.c", response.email());
        assertEquals(14 , response.age());
        assertEquals(UserStatus.ACTIVE.toString(), response.status());
    }

    @Test
    void shouldSuspendActiveUserSuccessfully() {
        // arrange — mockear findById con un usuario ACTIVE
        var mockedUser = buildMockSavedUser(1L,
                request.username(),
                request.firstName(),
                request.lastName(),
                request.phone(),
                request.email(),
                request.age(),
                UserStatus.ACTIVE);
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockedUser));
        // act — llamar a userService.suspendUser(id)
        var response = userService.suspendUser(1L);

        // assert — verificar que el status regresado sea "SUSPENDED"; confirmar que save fue llamado
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(any());
        assertEquals(1L, response.id());
        assertEquals("andypro", response.username());
        assertEquals("Andres", response.firstName());
        assertEquals("Silva", response.lastName());
        assertEquals("6682359422", response.phone());
        assertEquals("Andy#b.c", response.email());
        assertEquals(14 , response.age());
        assertEquals(UserStatus.SUSPENDED.toString(), response.status());
    }

    // ─── Validaciones de Username ─────────────────────────────────────────────

    @Test
    void shouldThrowWhenUsernameTooShort() {
        // Construir request con username de 4 caracteres
        UserController.UserRequest badRequest = new UserController.UserRequest("andy", "Andres", "Silva", "6682359422", "Andy#b.c", 14);

        // assertThrows InvalidUserDataException
        assertThrows(InvalidUserDataException.class, () -> userService.registerUser(badRequest));

    }

    @Test
    void shouldThrowWhenUsernameTooLong() {
        // construir request con username de 21 caracteres
        UserController.UserRequest badRequest = new UserController.UserRequest("andyprogamerrobloxmaster2005", "Andres", "Silva", "6682359422", "Andy#b.c", 14);

        // assertThrows InvalidUserDataException
        assertThrows(InvalidUserDataException.class, () -> userService.registerUser(badRequest));

    }

    @Test
    void shouldThrowWhenUsernameHasInvalidChars() {
        // username con mayúsculas o caracteres especiales, ej. "User@Name"
        UserController.UserRequest badRequest = new UserController.UserRequest("andy@pro", "Andres", "Silva", "6682359422", "Andy#b.c", 14);
        // assertThrows InvalidUserDataException
        assertThrows(InvalidUserDataException.class, () -> userService.registerUser(badRequest));

    }

    @Test
    void shouldThrowWhenUsernameStartsWithUnderscore() {
        // username "_nombrevalido"
        UserController.UserRequest badRequest = new UserController.UserRequest("_andypro", "Andres", "Silva", "6682359422", "Andy#b.c", 14);

        // assertThrows InvalidUserDataException
        assertThrows(InvalidUserDataException.class, () -> userService.registerUser(badRequest));
    }

    @Test
    void shouldThrowWhenUsernameEndsWithUnderscore() {
        // username "nombrevalido_"
        UserController.UserRequest badRequest = new UserController.UserRequest("andypro_", "Andres", "Silva", "6682359422", "Andy#b.c", 14);

        // assertThrows InvalidUserDataException
        assertThrows(InvalidUserDataException.class, () -> userService.registerUser(badRequest));

    }

    // ─── Validaciones de Nombre ───────────────────────────────────────────────

    @Test
    void shouldThrowWhenFirstNameTooShort() {
        // firstName de 1 carácter
        UserController.UserRequest badRequest = new UserController.UserRequest("andypro", "A", "Silva", "6682359422", "Andy#b.c", 14);

        // assertThrows InvalidUserDataException
        assertThrows(InvalidUserDataException.class, () -> userService.registerUser(badRequest));
    }

    @Test
    void shouldThrowWhenFirstNameContainsNumbers() {
        // firstName como "Juan5"
        UserController.UserRequest badRequest = new UserController.UserRequest("andypro", "Andres5", "Silva", "6682359422", "Andy#b.c", 14);

        // assertThrows InvalidUserDataException
        assertThrows(InvalidUserDataException.class, () -> userService.registerUser(badRequest));
    }

    @Test
    void shouldThrowWhenLastNameTooShort() {
        // lastName de 1 carácter
        UserController.UserRequest badRequest = new UserController.UserRequest("andypro", "Andres", "S", "6682359422", "Andy#b.c", 14);

        // assertThrows InvalidUserDataException
        assertThrows(InvalidUserDataException.class, () -> userService.registerUser(badRequest));
    }

    @Test
    void shouldThrowWhenLastNameContainsNumbers() {
        // lastName como "Perez2"
        UserController.UserRequest badRequest = new UserController.UserRequest("andypro", "Andres", "Silva5", "6682359422", "Andy#b.c", 14);

        // assertThrows InvalidUserDataException
        assertThrows(InvalidUserDataException.class, () -> userService.registerUser(badRequest));
    }

    // ─── Validaciones de Age ─────────────────────────────────────────────────

    @Test
    void shouldThrowWhenAgeIsExactlyTwelve() {
        // age = 12 — caso límite (boundary): debe ser MAYOR a 12, no mayor o igual
        UserController.UserRequest badRequest = new UserController.UserRequest("andypro", "Andres", "Silva", "6682359422", "Andy#b.c", 12);

        // assertThrows InvalidUserDataException
        assertThrows(InvalidUserDataException.class, () -> userService.registerUser(badRequest));
    }

    @Test
    void shouldThrowWhenAgeIsBelowTwelve() {
        // age = 5
        UserController.UserRequest badRequest = new UserController.UserRequest("andypro", "Andres", "Silva", "6682359422", "Andy#b.c", 5);

        // assertThrows InvalidUserDataException
        assertThrows(InvalidUserDataException.class, () -> userService.registerUser(badRequest));
    }

    @Test
    void shouldThrowWhenAgeExceedsMaximum() {
        // age = 121 — excede el máximo permitido de 120
        UserController.UserRequest badRequest = new UserController.UserRequest("andypro", "Andres", "Silva", "6682359422", "Andy#b.c", 121);

        // assertThrows InvalidUserDataException
        assertThrows(InvalidUserDataException.class, () -> userService.registerUser(badRequest));
    }

    // ─── Validaciones de Phone ───────────────────────────────────────────────

    @Test
    void shouldThrowWhenPhoneHasWrongLength() {
        // phone con 9 u 11 dígitos
        UserController.UserRequest badRequest = new UserController.UserRequest("andypro", "Andres", "Silva", "668235942", "Andy#b.c", 14);

        // assertThrows InvalidUserDataException
        assertThrows(InvalidUserDataException.class, () -> userService.registerUser(badRequest));
    }

    @Test
    void shouldThrowWhenPhoneContainsLetters() {
        // phone como "123456789a"
        UserController.UserRequest badRequest = new UserController.UserRequest("andypro", "Andres", "Silva", "123456789a", "Andy#b.c", 14);

        // assertThrows InvalidUserDataException
        assertThrows(InvalidUserDataException.class, () -> userService.registerUser(badRequest));
    }

    // ─── Validación de Email ──────────────────────────────────────────────────

    @Test
    void shouldThrowWhenEmailIsInvalid() {
        // mockear emailValidatorService.isValid(anyString()) para que regrese false

        when(emailValidatorService.isValid(anyString())).thenReturn(false);
        // assertThrows InvalidUserDataException
        assertThrows(InvalidUserDataException.class, () -> userService.registerUser(request));

        // verificar que emailValidatorService.isValid fue llamado (verify)
        verify(emailValidatorService).isValid("blabla");

    }

    // ─── Unicidad de Username ─────────────────────────────────────────────────

    @Test
    void shouldThrowWhenUsernameAlreadyExists() {
        // mockear emailValidatorService.isValid para que regrese true
        when(emailValidatorService.isValid(anyString())).thenReturn(true);
        // mockear userRepository.existsByUsername para que regrese true
        when(userRepository.existsByUsername(anyString())).thenReturn(true);
        // assertThrows DuplicateUsernameException
        assertThrows(DuplicateUsernameException.class, () -> userService.registerUser(request));

        // verificar que userRepository.save NUNCA fue llamado (verify never)
        verify(userRepository, never()).save(any());
    }

    // ─── Not found ───────────────────────────────────────────────────────────

    @Test
    void shouldThrowWhenUserNotFound() {
        // mockear userRepository.findById para que regrese Optional.empty()
        when(userRepository.findById(any())).thenReturn(Optional.empty());
        // assertThrows UserNotFoundException
        assertThrows(UserNotFoundException.class, () -> userService.registerUser(request));
    }

    @Test
    void shouldThrowWhenSuspendingAlreadySuspendedUser() {
        // mockear findById con un usuario SUSPENDED
        User user = new User();
        user.setId(1L);
        user.setUsername("andypro");
        user.setFirstName("Andres");
        user.setLastName("Silva");
        user.setPhone("6682359422");
        user.setEmail("Andy#b.c");
        user.setAge(14);
        user.setStatus(UserStatus.SUSPENDED);

        when(userRepository.findById(any())).thenReturn(Optional.of(user));

        // assertThrows InvalidUserDataException
        assertThrows(InvalidUserDataException.class, () -> userRepository.findById(1L));

    }
}
