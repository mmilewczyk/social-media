package pl.mmilewczyk.userservice.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.mmilewczyk.userservice.model.dto.UserEditRequest;
import pl.mmilewczyk.userservice.model.dto.UserResponseWithId;
import pl.mmilewczyk.userservice.model.enums.Gender;
import pl.mmilewczyk.userservice.service.UserService;
import pl.mmilewczyk.userservice.service.UtilsService;

@RestController
@CrossOrigin (origins = "*")
@RequestMapping("api/v1/users")
public record UserController(UserService userService, UtilsService utilsService) {

    @GetMapping
    public ResponseEntity<Page<UserResponseWithId>> getAllUsers(Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.getAllUsers(pageable));
    }

    @GetMapping("/profile")
    public ResponseEntity<UserResponseWithId> getLoggedInUser() {
        return ResponseEntity.status(HttpStatus.OK).body(utilsService.getLoggedInUser());
    }

    @GetMapping("/profile/{username}")
    ResponseEntity<UserResponseWithId> getUserByUsername(@PathVariable("username") String username) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.getUserByUsername(username));
    }

    @GetMapping("/search/id/{id}")
    public ResponseEntity<UserResponseWithId> getUserById(@PathVariable("id") Long userId) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.getUserByUserId(userId));
    }

    @PutMapping("/profile/edit")
    public ResponseEntity<UserResponseWithId> editExistingUser(@RequestBody UserEditRequest userEditRequest) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(userService.editExistingUser(userEditRequest));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<UserResponseWithId>> getUsersByFilter(@RequestParam(required = false) Gender gender,
                                                                     @RequestParam(required = false) String currentCity) {
        return ResponseEntity.ok(userService.getUsersByFilter(gender, currentCity));
    }
}

