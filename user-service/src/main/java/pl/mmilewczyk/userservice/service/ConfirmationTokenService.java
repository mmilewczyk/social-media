package pl.mmilewczyk.userservice.service;

import org.springframework.stereotype.Service;
import pl.mmilewczyk.userservice.model.entity.ConfirmationToken;
import pl.mmilewczyk.userservice.repository.ConfirmationTokenRepository;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public record ConfirmationTokenService(ConfirmationTokenRepository confirmationTokenRepository) {

    public void saveConfirmationToken(ConfirmationToken token) {
        confirmationTokenRepository.save(token);
    }

    public Optional<ConfirmationToken> getToken(String token) {
        return confirmationTokenRepository.findByToken(token);
    }

    public void setConfirmedAt(String token) {
        confirmationTokenRepository.updateConfirmedAt(token, LocalDateTime.now());
    }
}
