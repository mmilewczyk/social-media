package pl.mmilewczyk.userservice.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.mmilewczyk.userservice.model.dto.LoginRequest;
import pl.mmilewczyk.userservice.model.dto.UserResponseWithId;
import pl.mmilewczyk.userservice.service.LoginService;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping(path = "api/v1/login")
public record LoginController(LoginService loginService) {

    @PostMapping
    public UserResponseWithId login(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        return loginService.login(loginRequest, response);
    }
}
