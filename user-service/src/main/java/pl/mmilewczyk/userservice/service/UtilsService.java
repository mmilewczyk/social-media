package pl.mmilewczyk.userservice.service;

import io.jsonwebtoken.Jwts;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;
import pl.mmilewczyk.userservice.model.dto.UserResponseWithId;
import pl.mmilewczyk.userservice.model.entity.User;
import pl.mmilewczyk.userservice.repository.UserRepository;
import pl.mmilewczyk.userservice.security.JwtUtils;

import javax.servlet.http.HttpServletRequest;

@Service
public record UtilsService(UserRepository userRepository, JwtUtils jwtUtils) {

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
}
