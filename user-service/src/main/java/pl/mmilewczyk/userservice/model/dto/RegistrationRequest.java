package pl.mmilewczyk.userservice.model.dto;

/**
 * the class contains the fields that are required in the registration form
 */
public record RegistrationRequest(String username, String email, String password, String matchingPassword) {
}
