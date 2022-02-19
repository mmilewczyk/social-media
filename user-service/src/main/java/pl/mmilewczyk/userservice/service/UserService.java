package pl.mmilewczyk.userservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pl.mmilewczyk.userservice.model.dto.RankDTO;
import pl.mmilewczyk.userservice.model.dto.UserResponse;
import pl.mmilewczyk.userservice.model.dto.UserResponseWithId;
import pl.mmilewczyk.userservice.model.enums.RankName;
import pl.mmilewczyk.userservice.repository.UserRepository;

@Service
@Slf4j
public record UserService(UserRepository userRepository) {

    public Page<UserResponse> getAllUsers(Pageable pageable) {
        log.info("Get all of the users");
        //return userRepository.findAll(pageable);
        return null;
    }

    public UserResponseWithId getLoggedInUser() {
        return new UserResponseWithId(1l, "agiklo", "agiklo@mmilewczyk.pl", new RankDTO(RankName.GOLD, ""));
    }
}
