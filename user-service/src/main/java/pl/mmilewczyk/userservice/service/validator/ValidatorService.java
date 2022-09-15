package pl.mmilewczyk.userservice.service.validator;

import org.springframework.stereotype.Service;
import pl.mmilewczyk.userservice.model.dto.RegistrationRequest;
import pl.mmilewczyk.userservice.model.entity.User;
import pl.mmilewczyk.userservice.repository.UserRepository;

import static java.lang.String.format;

@Service
public record ValidatorService(PasswordValidator passwordValidator,
                               EmailValidator emailValidator,
                               UserRepository userRepository) {

    public void validatePasswordAndEmail(RegistrationRequest request) {
        boolean isValidEmail = emailValidator.validateEmail(request.email());
        boolean isPasswordValid = passwordValidator.validatePassword(request.password(), request.matchingPassword());

        if (!isValidEmail) throw new IllegalStateException(format("Email %s is not valid!", request.email()));
        if (!isPasswordValid) throw new IllegalStateException("Password is not valid!");
    }

    public void checkIfEmailOrUsernameAreNotTaken(User user) {
        boolean userExists = userRepository.findByUsername(user.getUsername()).isPresent();
        boolean emailExists = userRepository.findByEmail(user.getEmail()).isPresent();

        if (userExists) throw new IllegalStateException(format("Username %s already taken", user.getUsername()));
        if (emailExists) throw new IllegalStateException(format("Email %s already taken", user.getEmail()));
    }
}
