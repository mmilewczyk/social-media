package pl.mmilewczyk.userservice.service;

import io.jsonwebtoken.Jwts;
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
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;
import pl.mmilewczyk.userservice.model.dto.UserResponse;
import pl.mmilewczyk.userservice.model.dto.UserResponseWithId;
import pl.mmilewczyk.userservice.model.entity.User;
import pl.mmilewczyk.userservice.repository.UserRepository;
import pl.mmilewczyk.userservice.security.JwtUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public record UserService(
        UserRepository userRepository,
        ConfirmationTokenService confirmationTokenService,
        BCryptPasswordEncoder passwordEncoder,
        JwtUtils jwtUtils
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

    public UserResponseWithId getLoggedInUser() {
        String username = getUsernameFromJwtToken();
        User user = userRepository.findByUsername(username).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return new UserResponseWithId(user.getUserId(), user.getUsername(), user.getEmail(), user.getRank().name());
    }

    private String getUsernameFromJwtToken() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String token = request.getHeader("Authorization").split(" ")[1];
        return Jwts.parser()
                .setSigningKey(jwtUtils.getSecretKey())
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
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

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username).orElseThrow(() ->
                new UsernameNotFoundException(String.format(USER_NOT_FOUND_MSG, username)));
    }
}
