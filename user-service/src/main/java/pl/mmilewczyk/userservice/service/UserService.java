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
import pl.mmilewczyk.userservice.model.dto.UserEditRequest;
import pl.mmilewczyk.userservice.model.dto.UserResponseWithId;
import pl.mmilewczyk.userservice.model.entity.User;
import pl.mmilewczyk.userservice.model.enums.Gender;
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

    public Page<UserResponseWithId> getAllUsers(Pageable pageable) {
        List<UserResponseWithId> mappedUsers = new ArrayList<>();
        log.info("Search for all existing users from the database");
        for (User user : userRepository.findAll(pageable)) {
            mappedUsers.add(user.mapToUserResponseWithId());
        }
        return new PageImpl<>(mappedUsers);
    }

    public UserResponseWithId getLoggedInUser() {
        String username = getUsernameFromJwtToken();
        User user = userRepository.findByUsername(username).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return user.mapToUserResponseWithId();
    }

    private String getUsernameFromJwtToken() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String token = request.getHeader("Authorization").split(" ")[1];
        if (token == null) {
            throw new NullPointerException("Token is null");
        }
        return Jwts.parser()
                .setSigningKey(jwtUtils.getSecretKey())
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public UserResponseWithId getUserByUsername(String username) {
        log.debug("Searching the user with username: {}", username);
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format("User with username: %s does not exist", username)))
                .mapToUserResponseWithId();
    }

    public UserResponseWithId getUserById(Long userId) {
        log.debug("Searching the user with id: {}", userId);
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

    public UserResponseWithId editExistingUser(UserEditRequest userEditRequest) {
        log.debug("Searching the user with username: {}", userEditRequest.username());
        User user = userRepository.findByUsername(userEditRequest.username())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format("User with username: %s does not exist", userEditRequest.username())));
        log.debug("User with the username: {} has been found", userEditRequest.username());
        user.setUsername(userEditRequest.username());
        user.setFirstName(userEditRequest.firstName());
        user.setBirth(userEditRequest.birth());
        user.setGender(userEditRequest.gender());
        user.setCurrentCity(userEditRequest.currentCity());
        user.setHomeTown(userEditRequest.homeTown());
        user.setLanguagesISpeak(userEditRequest.languagesISpeak());
        user.setLanguagesImLearning(userEditRequest.languagesImLearning());
        user.setLookingFor(userEditRequest.lookingFor());
        user.setEducation(userEditRequest.education());
        user.setOccupationOrJob(userEditRequest.occupationOrJob());
        user.setRelationshipStatus(userEditRequest.relationshipStatus());
        user.setAboutMe(userEditRequest.aboutMe());

        Long loggedInUserId = getLoggedInUser().userId();
        log.debug("Checking if logged in user is the owner of the account");
        if (loggedInUserId.equals(user.getUserId())) {
            log.info("Saving the data: {} of the {}({}) user", userEditRequest, user.getUsername(), user.getUserId());
            userRepository.save(user);
            log.info("The edited {} data of {}({}) user has been saved", userEditRequest, user.getUsername(), user.getUserId());
        } else {
            log.info("The logged in user is not the owner of the account {}({})", user.getUsername(), user.getUserId());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You cannot edit someone else account");
        }
        log.debug("Mapping properties of {} to UserResponseWithId", user);
        return user.mapToUserResponseWithId();
    }

    public UserResponseWithId addUserToFriends(Long userId) {
        User loggedInUser = userRepository.findById(getLoggedInUser().userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format("User with id: %s does not exist", userId)));
        User potencialFriend = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format("User with id: %s does not exist", userId)));

        List<Long> friends = loggedInUser.getFriendsIds();
        friends.add(potencialFriend.getUserId());
        userRepository.save(loggedInUser);
        return loggedInUser.mapToUserResponseWithId();
    }

    public List<UserResponseWithId> getUsersByFilter(Gender gender, String currentCity) {
        List<User> users = userRepository.findAllByGenderOrCurrentCity(gender, currentCity);

        List<UserResponseWithId> mappedUsers = new ArrayList<>();
        users.forEach(user -> mappedUsers.add(user.mapToUserResponseWithId()));
        return mappedUsers;
    }

    //TODO: CREATE REPORT USER TO MODERATOR FUNCTION
}
