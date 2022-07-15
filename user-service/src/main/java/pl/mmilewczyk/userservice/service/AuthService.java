package pl.mmilewczyk.userservice.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import pl.mmilewczyk.clients.notification.NotificationClient;
import pl.mmilewczyk.userservice.model.dto.RegistrationRequest;
import pl.mmilewczyk.userservice.model.dto.auth.LoginRequest;
import pl.mmilewczyk.userservice.model.dto.auth.SuccessfulAuthDto;
import pl.mmilewczyk.userservice.model.entity.ConfirmationToken;
import pl.mmilewczyk.userservice.model.entity.User;
import pl.mmilewczyk.userservice.model.enums.RankName;
import pl.mmilewczyk.userservice.model.enums.RoleName;
import pl.mmilewczyk.userservice.repository.LanguageRepository;
import pl.mmilewczyk.userservice.repository.UserRepository;
import pl.mmilewczyk.userservice.security.JwtUtils;
import pl.mmilewczyk.userservice.service.validator.ValidatorService;

import javax.servlet.http.HttpServletResponse;
import java.nio.CharBuffer;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final ConfirmationTokenService confirmationTokenService;
    private final ValidatorService validatorService;
    private final JwtUtils jwtUtils;
    private final NotificationClient notificationClient;
    private final LanguageRepository languageRepository;

    public SuccessfulAuthDto signIn(LoginRequest loginRequest, HttpServletResponse response) {
        User user = userRepository.findByUsername(loginRequest.username())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (passwordEncoder.matches(CharBuffer.wrap(loginRequest.password()), user.getPassword())) {
            String token = createToken(user);
            SuccessfulAuthDto successfulAuthDto = new SuccessfulAuthDto(user.getUserId(), user.getUsername(), token);
            response.addHeader(jwtUtils.getAuthorizationHeader(), jwtUtils.getTokenPrefix() + token);
            return successfulAuthDto;
        }

        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid password");
    }

    public SuccessfulAuthDto validateToken(String token) {
        String username = Jwts.parser()
                .setSigningKey(jwtUtils.getSecretKey())
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
        Optional<User> userOptional = userRepository.findByUsername(username);

        if (userOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }

        User user = userOptional.get();
        return new SuccessfulAuthDto(user.getUserId(), user.getUsername(), createToken(user));
    }

    private String createToken(User user) {
        Claims claims = Jwts.claims().setSubject(user.getUsername());

        Date now = new Date();
        Date validity = new Date(now.getTime() + (jwtUtils.getTokenExpirationAfterDays() * 24 * 3600 * 1000));

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(SignatureAlgorithm.HS256, jwtUtils.getSecretKey())
                .compact();
    }

    public SuccessfulAuthDto signUp(RegistrationRequest request) {
        validatorService.validatePasswordAndEmail(request);

        String encodedPassword = passwordEncoder.encode(CharBuffer.wrap(request.password()));
        User user = new User(
                request.email(),
                request.username(),
                encodedPassword,
                RoleName.USER,
                RankName.BRONZE,
                request.firstName(),
                request.birthday(),
                request.gender(),
                request.currentCity(),
                request.hometown(),
                request.languageISpeak(),
                request.lookingFor());

        validatorService.checkIfEmailOrUsernameAreNotTaken(user);
        languageRepository.saveAllAndFlush(request.languageISpeak());
        userRepository.saveAndFlush(user);

        String token = UUID.randomUUID().toString();
        ConfirmationToken confirmationToken = new ConfirmationToken(token, LocalDateTime.now(), LocalDateTime.now().plusMinutes(15), user);
        confirmationTokenService.saveConfirmationToken(confirmationToken);

        String link = String.format("http://localhost:8082/api/v1/auth/confirm?token=%s", token);
        notificationClient.sendAccountConfirmationEmail(user.getEmail(), link);

        return new SuccessfulAuthDto(user.getUserId(), user.getUsername(), token);
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
        enableUser(confirmationToken.getUser().getEmail());
        return "confirmed";
    }

    public void enableUser(String username) {
        userRepository.enableUser(username);
    }

    //only for development purpose
    public void deleteAllUsers() {
        confirmationTokenService.confirmationTokenRepository().deleteAll();
        userRepository.deleteAll();
    }
}
