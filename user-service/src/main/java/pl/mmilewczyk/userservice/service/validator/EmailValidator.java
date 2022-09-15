package pl.mmilewczyk.userservice.service.validator;

import org.springframework.stereotype.Component;

import java.util.function.Predicate;

import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.regex.Pattern.compile;

@Component
public class EmailValidator implements Predicate<String> {

    @Override
    public boolean test(String email) {
        return validateEmail(email);
    }

    public boolean validateEmail(String email) {
        return compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", CASE_INSENSITIVE)
                .matcher(email)
                .matches();
    }
}
