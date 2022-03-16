package pl.mmilewczyk.userservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import pl.mmilewczyk.userservice.model.dto.UserResponse;
import pl.mmilewczyk.userservice.model.dto.UserResponseWithId;
import pl.mmilewczyk.userservice.model.entity.User;
import pl.mmilewczyk.userservice.repository.UserRepository;
import pl.mmilewczyk.userservice.model.entity.ConfirmationToken;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public record UserService(
        UserRepository userRepository,
        ConfirmationTokenService confirmationTokenService,
        BCryptPasswordEncoder passwordEncoder
) implements UserDetailsService {

    private static final String USER_NOT_FOUND_MSG = "user with username %s not found";

    public Page<UserResponse> getAllUsers(Pageable pageable) {
        List<UserResponse> mappedUsers = new ArrayList<>();
        log.info("Getting all of the users from database");
        for (User user : userRepository.findAll(pageable)) {
            mappedUsers.add(user.mapToUserResponse());
        }
        return new PageImpl<>(mappedUsers);
    }

    public UserResponseWithId getLoggedInUser(Principal principal) {
        User user = userRepository.findByUsername(principal.getName()).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return new UserResponseWithId(user.getUserId(), user.getUsername(), user.getEmail(), user.getRank().name());
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

    public int enableUser(String username) {
        return userRepository.enableUser(username);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username).orElseThrow(() ->
                new UsernameNotFoundException(String.format(USER_NOT_FOUND_MSG, username)));
    }

    public String signUpUser(User user) {
        checkIfEmailOrUsernameAreNotTaken(user);

        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);
        userRepository.save(user);

        String token = UUID.randomUUID().toString();
        ConfirmationToken confirmationToken = new ConfirmationToken(
                token,
                LocalDateTime.now(),
                LocalDateTime.now().plusMinutes(15),
                user);

        confirmationTokenService.saveConfirmationToken(confirmationToken);

        return token;
    }

    private void checkIfEmailOrUsernameAreNotTaken(User user) {
        boolean userExists = userRepository.findByUsername(user.getUsername()).isPresent();
        boolean emailExists = userRepository.findByEmail(user.getEmail()).isPresent();

        if (userExists) {
            throw new IllegalStateException(String.format("Username %s already taken", user.getUsername()));
        }
        if (emailExists) {
            throw new IllegalStateException(String.format("Email %s already taken", user.getEmail()));
        }
    }
}
