package pl.mmilewczyk.clients.user;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("USER-SERVICE")
public interface UserClient {

    @GetMapping("api/v1/users/profile")
    ResponseEntity<UserResponseWithId> getLoggedInUser();

    @GetMapping("api/v1/users/{username}")
    ResponseEntity<UserResponseWithId> getUserByUsername(@PathVariable("username") String username);

    @GetMapping("api/v1/users")
    ResponseEntity<UserResponseWithId> getUserById(@RequestParam("id") Long userId);

    @GetMapping("api/v1/users/profile/{userId}/followed")
    ResponseEntity<Page<UserResponseWithId>> getFollowedUsersOfUserByUserId(@PathVariable("userId") Long userId);
}
