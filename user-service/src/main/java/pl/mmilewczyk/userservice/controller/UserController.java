package pl.mmilewczyk.userservice.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.mmilewczyk.userservice.model.dto.UserResponse;
import pl.mmilewczyk.userservice.model.dto.UserResponseWithId;
import pl.mmilewczyk.userservice.service.UserService;

import java.security.Principal;

@RestController
@RequestMapping("api/v1/users")
public record UserController(UserService userService) {

    @GetMapping
    public ResponseEntity<Page<UserResponse>> getAllUsers(Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.getAllUsers(pageable));
    }

    @GetMapping("/loggedInUser")
    public ResponseEntity<UserResponseWithId> getLoggedInUser(Principal principal) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.getLoggedInUser(principal));
    }

    @GetMapping("/{username}")
    ResponseEntity<UserResponseWithId> getUserByUsername(@PathVariable("username") String username) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.getUserByUsername(username));
    }

    @GetMapping("/search/id/{id}")
    ResponseEntity<UserResponseWithId> getUserById(@PathVariable("id") Long userId) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.getUserById(userId));
    }
}
