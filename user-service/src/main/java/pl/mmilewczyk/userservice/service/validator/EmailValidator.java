package pl.mmilewczyk.userservice.service.validator;

import org.springframework.stereotype.Component;

import java.util.function.Predicate;
import java.util.regex.Pattern;

@Component
public class EmailValidator implements Predicate<String> {

    @Override
    public boolean test(String email) {
        return validateEmail(email);
    }

    public boolean validateEmail(String email) {
        return Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE)
                .matcher(email)
                .matches();
    }
}
