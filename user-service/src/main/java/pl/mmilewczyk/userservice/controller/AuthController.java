package pl.mmilewczyk.userservice.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.mmilewczyk.userservice.model.dto.RegistrationRequest;
import pl.mmilewczyk.userservice.model.dto.auth.LoginRequest;
import pl.mmilewczyk.userservice.model.dto.auth.SuccessfulAuthDto;
import pl.mmilewczyk.userservice.service.AuthService;

import javax.servlet.http.HttpServletResponse;

import static org.springframework.http.ResponseEntity.ok;

@Slf4j
@RestController
@CrossOrigin(origins = "*")
@RequestMapping(path = "api/v1/auth")
public record AuthController(AuthService authService) {

    @PostMapping("/signIn")
    public ResponseEntity<SuccessfulAuthDto> signIn(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        log.info("Trying to login {}", loginRequest.username());
        return ok(authService.signIn(loginRequest, response));
    }

    @PostMapping("/validateToken")
    public ResponseEntity<SuccessfulAuthDto> validateToken(@RequestParam("token") String token) {
        log.info("Trying to validate token {}", token);
        return ok(authService.validateToken(token));
    }

    @GetMapping("/confirm")
    public ResponseEntity<String> confirmAccount(@RequestParam("token") String token) {
        log.info("Trying to confirm account by token {}", token);
        return ok(authService.confirmToken(token));
    }

    @PostMapping("/signUp")
    public ResponseEntity<SuccessfulAuthDto> createNewAccount(@RequestBody RegistrationRequest registrationRequest) {
        log.info("Creating new user {}", registrationRequest.username());
        return ok(authService.signUp(registrationRequest));
    }

    @DeleteMapping("/deleteAll")
    public void deleteAllUsers() {
        authService.deleteAllUsers();
    }
}
