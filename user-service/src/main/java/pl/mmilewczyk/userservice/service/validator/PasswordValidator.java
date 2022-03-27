package pl.mmilewczyk.userservice.service.validator;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

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
        boolean isValid = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{8,}$")
                .matcher(password)
                .matches();

        if (!isValid) {
            throw new IllegalStateException("""
                    Password is too weak, password should contain:
                    - minimum eight characters,
                    - at least one uppercase letter,
                    - one lowercase letter,
                    - one number
                    """);
        } else {
            return isValid;
        }
    }
}
