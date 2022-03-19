package pl.mmilewczyk.userservice.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import pl.mmilewczyk.userservice.model.dto.LoginRequest;
import pl.mmilewczyk.userservice.model.dto.UserResponseWithId;
import pl.mmilewczyk.userservice.model.entity.User;
import pl.mmilewczyk.userservice.repository.UserRepository;
import pl.mmilewczyk.userservice.security.jwt.JwtConfiguration;

import javax.crypto.SecretKey;
import javax.servlet.http.HttpServletResponse;
import java.nio.CharBuffer;
import java.time.LocalDate;
import java.util.Date;

@Service
public record LoginService(UserRepository userRepository,
                           BCryptPasswordEncoder passwordEncoder,
                           JwtConfiguration jwtConfiguration,
                           SecretKey secretKey) {

    public UserResponseWithId login(LoginRequest loginRequest, HttpServletResponse response) {
        User user = userRepository.findByUsername(loginRequest.username())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (passwordEncoder.matches(CharBuffer.wrap(loginRequest.password()), user.getPassword())) {
            String token = createToken(user);
            UserResponseWithId userResponseWithId = validateToken(token);
            response.addHeader(jwtConfiguration.getAuthorizationHeader(), jwtConfiguration.getTokenPrefix() + token);
            return userResponseWithId;
        }
        throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Invalid password");
    }

    private UserResponseWithId validateToken(String token) {
        String login = Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
        User user = userRepository.findByUsername(login)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return new UserResponseWithId(
                user.getUserId(),
                user.getUsername(),
                user.getEmail(),
                user.getRank().name());
    }

    private String createToken(User user) {
        Claims claims = Jwts.claims().setSubject(user.getUsername());
        claims.put("authorities", user.getAuthorities());
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(java.sql.Date.valueOf(LocalDate.now().plusWeeks(2)))
                .signWith(secretKey)
                .compact();
    }
}
