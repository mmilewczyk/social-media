package pl.mmilewczyk.userservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import pl.mmilewczyk.userservice.model.dto.RankDTO;
import pl.mmilewczyk.userservice.model.dto.UserResponse;
import pl.mmilewczyk.userservice.model.dto.UserResponseWithId;
import pl.mmilewczyk.userservice.model.entity.User;
import pl.mmilewczyk.userservice.model.enums.RankName;
import pl.mmilewczyk.userservice.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public record UserService(UserRepository userRepository) {

    public Page<UserResponse> getAllUsers(Pageable pageable) {
        List<UserResponse> mappedUsers = new ArrayList<>();
        log.info("Getting all of the users from database");
        for (User user : userRepository.findAll(pageable)) {
            mappedUsers.add(user.mapToUserResponse());
        }
        return new PageImpl<>(mappedUsers);
    }

    public UserResponseWithId getLoggedInUser() {
        // TODO: implement getting logged in user
        return new UserResponseWithId(1L, "agiklo", "agiklo@mmilewczyk.pl", new RankDTO(RankName.GOLD, ""));
    }

    public UserResponseWithId getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format("User with username: %s does not exist", username)))
                .mapToUserResponseWithId();
    }

    public UserResponseWithId getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format("User with id: %s does not exist", userId)))
                .mapToUserResponseWithId();
    }
}
