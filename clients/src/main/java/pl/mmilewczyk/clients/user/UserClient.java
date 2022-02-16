package pl.mmilewczyk.clients.user;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient("user-service")
public interface UserClient {

    @GetMapping("api/v1/users/loggedInUser")
    ResponseEntity<UserResponseWithId> getLoggedInUser();
}
