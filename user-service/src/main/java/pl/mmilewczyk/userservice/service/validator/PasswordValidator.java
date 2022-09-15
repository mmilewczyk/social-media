package pl.mmilewczyk.userservice.service.validator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static java.util.regex.Pattern.compile;

@Slf4j
@Component
public class PasswordValidator {

    public boolean validatePassword(String password, String matchingPassword) {
        boolean isNotWeak = checkIfPasswordIsNotTooWeak(password);
        boolean areEquals = checkIfPasswordsAreEquals(password, matchingPassword);
        return isNotWeak && areEquals;
    }

    private boolean checkIfPasswordsAreEquals(String password, String matchingPassword) {
        return password.equals(matchingPassword);
    }

    private boolean checkIfPasswordIsNotTooWeak(String password) {
        boolean isValid = compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{8,}$")
                .matcher(password)
                .matches();
        if (!isValid) {
            log.error("""
                    Password is too weak, password should contain:
                    - minimum eight characters,
                    - at least one uppercase letter,
                    - one lowercase letter,
                    - one number
                    """);
        }
        return isValid;
    }
}
