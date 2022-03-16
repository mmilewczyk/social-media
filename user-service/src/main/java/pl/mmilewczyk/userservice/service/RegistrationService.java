package pl.mmilewczyk.userservice.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import pl.mmilewczyk.userservice.model.entity.User;
import pl.mmilewczyk.userservice.model.enums.RankName;
import pl.mmilewczyk.userservice.model.enums.RoleName;
import pl.mmilewczyk.userservice.model.dto.RegistrationRequest;
import pl.mmilewczyk.userservice.model.entity.ConfirmationToken;

import java.time.LocalDateTime;

@Slf4j
@Service
@AllArgsConstructor
public class RegistrationService {

    private final UserService userService;
    private final EmailValidator emailValidator;
    private final PasswordValidator passwordValidator;
    private final ConfirmationTokenService confirmationTokenService;

    public String register(RegistrationRequest request) {
        boolean isValidEmail = emailValidator.validateEmail(request.email());
        boolean isPasswordValid = passwordValidator.validatePassword(request.password(), request.matchingPassword());

        if (!isValidEmail) {
            throw new IllegalStateException(String.format("Email %s is not valid!", request.email()));
        }

        if (!isPasswordValid) {
            throw new IllegalStateException("Password is not valid!");
        }

        String token = userService.signUpUser(new User(request.email(), request.username(), request.password(), RoleName.USER, RankName.BROWN));
        String link = String.format("ACTIVATE ACCOUNT BY LINK: http://localhost:8082/api/v1/registration/confirm?token=%s", token);
        log.info(link);
        return token;
    }

    @Transactional
    public String confirmToken(String token) {
        ConfirmationToken confirmationToken = confirmationTokenService.getToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "token not found"));

        if (confirmationToken.getConfirmedAt() != null) {
            throw new ResponseStatusException(HttpStatus.GONE, "Account already confirmed");
        }

        LocalDateTime expiredAt = confirmationToken.getExpiresAt();

        if (expiredAt.isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("token expired");
        }

        confirmationTokenService.setConfirmedAt(token);
        userService.enableUser(confirmationToken.getUser().getEmail());
        return "confirmed";
    }
}
