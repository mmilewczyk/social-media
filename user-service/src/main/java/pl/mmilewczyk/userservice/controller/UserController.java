package pl.mmilewczyk.userservice.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.mmilewczyk.userservice.model.dto.UserEditRequest;
import pl.mmilewczyk.userservice.model.dto.UserResponseWithId;
import pl.mmilewczyk.userservice.model.enums.Gender;
import pl.mmilewczyk.userservice.service.UserService;
import pl.mmilewczyk.userservice.service.UtilsService;

import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.http.ResponseEntity.status;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("api/v1/users")
public record UserController(UserService userService, UtilsService utilsService) {

    @GetMapping
    public ResponseEntity<Page<UserResponseWithId>> getAllUsers(Pageable pageable) {
        return status(OK).body(userService.getAllUsers(pageable));
    }

    @GetMapping("/profile")
    @ResponseStatus(OK)
    public UserResponseWithId getLoggedInUser() {
        return utilsService.getLoggedInUser();
    }

    @GetMapping("/profile/{username}")
    ResponseEntity<UserResponseWithId> getUserByUsername(@PathVariable("username") String username) {
        return status(OK).body(userService.getUserByUsername(username));
    }

    @GetMapping("/search/id/{id}")
    public ResponseEntity<UserResponseWithId> getUserById(@PathVariable("id") Long userId) {
        return status(OK).body(userService.getUserByUserId(userId));
    }

    @PutMapping("/profile/edit")
    public ResponseEntity<UserResponseWithId> editExistingUser(@RequestBody UserEditRequest userEditRequest) {
        return status(ACCEPTED).body(userService.editExistingUser(userEditRequest));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<UserResponseWithId>> getUsersByFilter(@RequestParam(required = false) Gender gender,
                                                                     @RequestParam(required = false) String currentCity) {
        return ok(userService.getUsersByFilter(gender, currentCity));
    }
}

