package pl.mmilewczyk.userservice.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Base64;

@Component
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JwtUtils {

    @Value("${jwt.private-key}")
    private String secretKey;

    @Value("${jwt.tokenExpirationAfterDays}")
    private Integer tokenExpirationAfterDays;

    @Value("${jwt.authorizationHeader}")
    private String authorizationHeader;

    @Value("${jwt.tokenPrefix}")
    private String tokenPrefix;

    @PostConstruct
    protected void init() {
        secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
    }
}
