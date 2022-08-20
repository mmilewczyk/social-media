package pl.mmilewczyk.clients.user;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient("USER-SERVICE")
public interface UserClient {

    @GetMapping("api/v1/users/profile/{username}")
    ResponseEntity<UserResponseWithId> getUserByUsername(@PathVariable("username") String username);

    @GetMapping("api/v1/users/search/id/{id}")
    ResponseEntity<UserResponseWithId> getUserById(@PathVariable("id") Long userId);

    @GetMapping("api/v1/users/profile/{userId}/followed")
    ResponseEntity<Page<UserResponseWithId>> getFollowedUsersOfUserByUserId(@PathVariable("userId") Long userId);

    @GetMapping("api/v1/users/profile/technical/{userId}/followed")
    List<UserResponseWithId> technicalGetFollowedUsersOfUserByUserId(@PathVariable("userId") Long userId);
}
