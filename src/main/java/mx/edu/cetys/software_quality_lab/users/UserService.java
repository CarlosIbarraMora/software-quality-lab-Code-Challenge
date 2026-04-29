package mx.edu.cetys.software_quality_lab.users;

import mx.edu.cetys.software_quality_lab.users.exceptions.DuplicateUsernameException;
import mx.edu.cetys.software_quality_lab.users.exceptions.InvalidUserDataException;
import mx.edu.cetys.software_quality_lab.users.exceptions.UserNotFoundException;
import mx.edu.cetys.software_quality_lab.validators.EmailValidatorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
import java.util.Optional;

@Service
public class UserService {

    private final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final EmailValidatorService emailValidatorService;

    public UserService(UserRepository userRepository, EmailValidatorService emailValidatorService) {
        this.userRepository = userRepository;
        this.emailValidatorService = emailValidatorService;
    }

    /**
     * Registrar un nuevo usuario aplicando todas las reglas de negocio.
     *
     * Reglas a implementar (lanzar InvalidUserDataException a menos que se indique):
     *  1. Username  — entre 5 y 20 caracteres, solo letras minúsculas, dígitos y guion bajo (_),
     *                 NO debe comenzar ni terminar con guion bajo
     *  2. First name — entre 2 y 50 caracteres, solo letras (se permiten acentos: á, é, ñ, etc.)
     *  3. Last name  — entre 2 y 50 caracteres, solo letras (se permiten acentos)
     *  4. Age        — debe ser mayor a 12 y menor o igual a 120
     *  5. Phone      — exactamente 10 dígitos, sin letras ni símbolos
     *  6. Email      — delegar a emailValidatorService.isValid(email);
     *                  lanzar InvalidUserDataException si regresa false
     *  7. Unicidad del username — si userRepository.existsByUsername regresa true,
     *                             lanzar DuplicateUsernameException
     */
    UserController.UserResponse registerUser(UserController.UserRequest request) {
        log.info("Iniciando registro de usuario, username={}", request.username());
        // TODO: implementar las reglas 1-7, luego guardar en BD y mapear la respuesta
        String username = request.username();

        //1.First Rule
        if((username.length() < 5 || username.length() > 20) || (!checkStartAndEnd(username) || (!allIsLowerCaseOrDigitOrGuion(username)))){
            throw new InvalidUserDataException("The username is invalid");
        }

        //2. Second Rule
        String firstName = request.firstName();
        if((firstName.length() < 2 || firstName.length() > 50) || (!allIsLetters(firstName))){
            throw new InvalidUserDataException("The first name is not valid");
        }

        //3. Third Rule
        String lastName = request.lastName();
        if((lastName.length() < 2 || lastName.length() > 50) || (!allIsLetters(lastName))){
            throw new InvalidUserDataException("The last name is not valid");
        }

        //4. Four Rule
        if(request.age() < 12 || request.age() > 120){
            throw new InvalidUserDataException("The age is not valid");
        }

        //5. Fifth rule
        String phone = request.phone();
        if((phone.length() != 10) || (!validPhoneNumber(phone))){
            throw new InvalidUserDataException("The phone number is not valid");
        }

        //6. Fifth Rule
        if(!emailValidatorService.isValid(request.email())){
            throw new InvalidUserDataException("The email is not validate");
        }

        //7. Rule
        if(userRepository.existsByUsername(username)){
            throw new DuplicateUsernameException("This user already exists, is duplicated");
        }

        User newUser = new User();
        newUser.setUsername(request.username());
        newUser.setFirstName(request.firstName());
        newUser.setLastName(request.lastName());
        newUser.setAge(request.age());
        newUser.setPhone(request.phone());
        newUser.setEmail(request.email());
        newUser.setStatus(UserStatus.ACTIVE);

        var savedUser = userRepository.save(newUser);
        return mapToResponse(savedUser);
    }

    /**
     * Buscar un usuario por ID.
     * Lanzar UserNotFoundException (HTTP 404) si el usuario no existe.
     */
    UserController.UserResponse getUserById(Long id) {
        log.info("Buscando usuario por ID, id={}", id);
        // TODO: buscar por id con findById, lanzar UserNotFoundException si está vacío, mapear y regresar
        Optional<User> foundedUser = userRepository.findById(id);
        if(foundedUser.isEmpty()){
            throw new UserNotFoundException("The user with ID : " + id + "wasnt found");
        }

        var user = foundedUser.get();
        return mapToResponse(user);
    }

    /**
     * Suspender un usuario ACTIVO.
     * Lanzar UserNotFoundException si el usuario no existe.
     * Lanzar InvalidUserDataException si el usuario ya está SUSPENDED.
     */
    UserController.UserResponse suspendUser(Long id) {
        log.info("Suspendiendo usuario, id={}", id);
        // TODO: buscar usuario, validar status, cambiar a SUSPENDED, guardar, mapear y regresar
        throw new UnsupportedOperationException("TODO: implementar suspendUser");
    }

    private UserController.UserResponse mapToResponse(User user) {
        // TODO: mapear los campos de la Entity User al record UserController.UserResponse
        return new UserController.UserResponse(
                user.getId(),
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhone(),
                user.getEmail(),
                user.getAge(),
                user.getStatus()
        );
    }

    private boolean checkStartAndEnd(String username){
        return (username.charAt(0) != '_') && (username.charAt(username.length() - 1) != '_');
    }

    private boolean allIsLowerCaseOrDigitOrGuion(String username){
        for(char c : username.toCharArray()){
            if(!Character.isLowerCase(c) || !Character.isDigit(c) || c != '_'){
                return false;
            }
        }

        return true;
    }

    private boolean allIsLetters(String name){
        for(char c : name.toCharArray()){
            if(!Character.isLetter(c)){
                return false;
            }
        }

        return true;
    }

    private boolean validPhoneNumber(String phoneNumber){
        for(char c : phoneNumber.toCharArray()){
            if(!Character.isDigit(c)){
                return false;
            }
        }

        return true;
    }
}