package pl.mmilewczyk.userservice.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.mmilewczyk.userservice.model.dto.LoginRequest;
import pl.mmilewczyk.userservice.model.dto.RegistrationRequest;
import pl.mmilewczyk.userservice.model.dto.SuccessfulAuthDto;
import pl.mmilewczyk.userservice.service.AuthService;

import javax.servlet.http.HttpServletResponse;

@Slf4j
@RestController
@RequestMapping(path = "api/v1/auth")
public record AuthController(AuthService authService) {

    @PostMapping("/signIn")
    public ResponseEntity<SuccessfulAuthDto> signIn(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        log.info("Trying to login {}", loginRequest.username());
        return ResponseEntity.ok(authService.signIn(loginRequest, response));
    }

    @PostMapping("/validateToken")
    public ResponseEntity<SuccessfulAuthDto> validateToken(@RequestParam("token") String token) {
        log.info("Trying to validate token {}", token);
        return ResponseEntity.ok(authService.validateToken(token));
    }

    @GetMapping("/confirm")
    public ResponseEntity<String> confirmAccount(@RequestParam("token") String token) {
        log.info("Trying to confirm account by token {}", token);
        return ResponseEntity.ok(authService.confirmToken(token));
    }

    @PostMapping("/signUp")
    public ResponseEntity<SuccessfulAuthDto> createNewAccount(@RequestBody RegistrationRequest registrationRequest) {
        log.info("Creating new user {}", registrationRequest.username());
        return ResponseEntity.ok(authService.signUp(registrationRequest));
    }

    @DeleteMapping("/deleteAll")
    public void deleteAllUsers() {
        authService.deleteAllUsers();
    }
}
