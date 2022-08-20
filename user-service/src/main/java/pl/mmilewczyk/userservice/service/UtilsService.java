package pl.mmilewczyk.userservice.service;

import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;
import pl.mmilewczyk.userservice.model.dto.UserResponseWithId;
import pl.mmilewczyk.userservice.model.entity.User;
import pl.mmilewczyk.userservice.repository.UserRepository;
import pl.mmilewczyk.userservice.security.JwtUtils;

import javax.servlet.DispatcherType;
import javax.servlet.http.HttpServletRequest;
import java.util.Objects;


@Service
@RequiredArgsConstructor
public final class UtilsService {

    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;

    @Value("${test.user.nickname}")
    private String loggedUserTest;

    public UserResponseWithId getLoggedInUser() {
        String username = getUsernameFromJwtToken();
        User user = userRepository.findByUsername(username).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return user.mapToUserResponseWithId();
    }

    private String getUsernameFromJwtToken() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        DispatcherType type = request.getDispatcherType();
        String token = null;
        if (type == DispatcherType.FORWARD) {
            token = (String) request.getAttribute("Authorization");
            if (token != null) {
                token = token.split(" ")[1];
            }
        } else if (type == DispatcherType.REQUEST) {
            token = request.getHeader("Authorization");
            if (token != null) {
                token = token.split(" ")[1];
            }
        }
        if (token == null) {
            throw new NullPointerException("Token is null");
        }
        return Jwts.parser()
                .setSigningKey(jwtUtils.getSecretKey())
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public UserRepository userRepository() {
        return userRepository;
    }

    public JwtUtils jwtUtils() {
        return jwtUtils;
    }

    @Value("${value.from.file}")
    public String loggedUserTest() {
        return loggedUserTest;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (UtilsService) obj;
        return Objects.equals(this.userRepository, that.userRepository) &&
                Objects.equals(this.jwtUtils, that.jwtUtils) &&
                Objects.equals(this.loggedUserTest, that.loggedUserTest);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userRepository, jwtUtils, loggedUserTest);
    }

    @Override
    public String toString() {
        return "UtilsService[" +
                "userRepository=" + userRepository + ", " +
                "jwtUtils=" + jwtUtils + ", " +
                "loggedUserTest=" + loggedUserTest + ']';
    }

}
