package pl.mmilewczyk.apigateway.config;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import pl.mmilewczyk.clients.user.UserResponseWithId;

import static java.lang.String.valueOf;
import static java.util.Objects.requireNonNull;
import static org.springframework.cloud.openfeign.security.OAuth2FeignRequestInterceptor.BEARER;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Component
public class AuthGatewayFilter extends AbstractGatewayFilterFactory<AuthGatewayFilter.Config> {

    private final WebClient.Builder webClientBuilder;

    public AuthGatewayFilter(WebClient.Builder webClientBuilder) {
        super(Config.class);
        this.webClientBuilder = webClientBuilder;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            if (!exchange.getRequest().getHeaders().containsKey(AUTHORIZATION)) {
                throw new ResponseStatusException(UNAUTHORIZED, "Missing authorization information");
            }

            String authHeader = requireNonNull(exchange.getRequest().getHeaders().get(AUTHORIZATION)).get(0);

            String[] parts = authHeader.split(" ");

            if (parts.length != 2 || !BEARER.equals(parts[0])) {
                throw new RuntimeException("Incorrect authorization structure");
            }

            return webClientBuilder.build()
                    .post()
                    .uri("http://user-service/api/v1/auth/validateToken?token=" + parts[1])
                    .retrieve().bodyToMono(UserResponseWithId.class)
                    .map(user -> {
                        exchange.getRequest()
                                .mutate()
                                .header("X-auth-user-id", valueOf(user.userId()));
                        return exchange;
                    })
                    .flatMap(chain::filter);
        };
    }


    public static class Config {

    }
}
