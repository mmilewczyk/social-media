package pl.mmilewczyk.userservice.service;

import org.springframework.stereotype.Service;
import pl.mmilewczyk.userservice.model.entity.ConfirmationToken;
import pl.mmilewczyk.userservice.repository.ConfirmationTokenRepository;

import java.util.Optional;

import static java.time.LocalDateTime.now;

@Service
public record ConfirmationTokenService(ConfirmationTokenRepository confirmationTokenRepository) {

    public void saveConfirmationToken(ConfirmationToken token) {
        confirmationTokenRepository.save(token);
    }

    public Optional<ConfirmationToken> getToken(String token) {
        return confirmationTokenRepository.findByToken(token);
    }

    public void setConfirmedAt(String token) {
        confirmationTokenRepository.updateConfirmedAt(token, now());
    }
}
