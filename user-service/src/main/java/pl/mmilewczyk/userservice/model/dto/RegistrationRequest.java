package pl.mmilewczyk.userservice.model.dto;

import pl.mmilewczyk.userservice.model.entity.Language;
import pl.mmilewczyk.userservice.model.enums.Gender;
import pl.mmilewczyk.userservice.model.enums.LookingFor;

import java.time.LocalDate;
import java.util.List;

public record RegistrationRequest(String username,
                                  String email,
                                  String password,
                                  String matchingPassword,
                                  String firstName,
                                  LocalDate birthday,
                                  Gender gender,
                                  String currentCity,
                                  String hometown,
                                  List<Language> languageISpeak,
                                  List<LookingFor> lookingFor) {
}
