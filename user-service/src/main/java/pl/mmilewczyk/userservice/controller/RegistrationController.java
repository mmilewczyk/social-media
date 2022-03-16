package pl.mmilewczyk.userservice.controller;

import lombok.AllArgsConstructor;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.web.bind.annotation.*;
import pl.mmilewczyk.userservice.model.dto.RegistrationRequest;
import pl.mmilewczyk.userservice.service.RegistrationService;

@RestController
@RequestMapping(path = "api/v1/registration")
@AllArgsConstructor
@EnableWebSecurity
public class RegistrationController {

    private final RegistrationService registrationService;

    @PostMapping
    public String createNewAccount(@RequestBody RegistrationRequest request) {
        return registrationService.register(request);
    }

    @GetMapping("/confirm")
    public String confirmAccount(@RequestParam("token") String token) {
        return registrationService.confirmToken(token);
    }
}
