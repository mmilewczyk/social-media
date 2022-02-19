package pl.mmilewczyk.clients.user;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("USER-SERVICE")
public interface UserClient {

    @GetMapping("api/v1/users/loggedInUser")
    ResponseEntity<UserResponseWithId> getLoggedInUser();

    @GetMapping("api/v1/users/{username}")
    ResponseEntity<UserResponseWithId> getUserByUsername(@PathVariable("username") String username);
}
