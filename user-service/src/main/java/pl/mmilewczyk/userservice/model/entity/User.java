package pl.mmilewczyk.userservice.model.entity;

import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import pl.mmilewczyk.userservice.model.dto.UserResponseWithId;
import pl.mmilewczyk.userservice.model.enums.*;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.Period;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Builder
@AllArgsConstructor
@Entity
@Table(name = "Users")
public class User implements UserDetails {

    @Id
    @SequenceGenerator(name = "user_id_sequence", sequenceName = "user_id_sequence")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_id_sequence")
    private Long userId;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(name = "is_locked")
    private Boolean isLocked = false;

    @Column(name = "is_enabled")
    private Boolean isEnabled = false;

    @Enumerated(EnumType.STRING)
    @JoinColumn(name = "user_role")
    private RoleName userRole;

    @Enumerated(EnumType.STRING)
    private RankName rank;

    private String aboutMe;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private LocalDate birth;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(nullable = false)
    private String currentCity;

    @Column(nullable = false)
    private String homeTown;

    @OneToMany
    private List<Language> languagesImLearning;

    @OneToMany
    @Column(nullable = false)
    private List<Language> languagesISpeak;

    @ElementCollection
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private List<LookingFor> lookingFor;

    @ManyToOne
    @JoinColumn(name = "education_id")
    private Education education;

    private String occupationOrJob;

    @Enumerated(EnumType.STRING)
    private RelationshipStatus relationshipStatus;

    private Long followersAmount;

    private Long followedAmount;


    private Boolean notifyAboutComments;

    public User(String email, String username, String password, RoleName userRole, RankName rank) {
        this.email = email;
        this.username = username;
        this.password = password;
        this.userRole = userRole;
        this.rank = rank;
    }

    public User(String email,
                String username,
                String password,
                RoleName userRole,
                RankName rank,
                String firstName,
                LocalDate birth,
                Gender gender,
                String currentCity,
                String homeTown,
                List<Language> languagesISpeak,
                List<LookingFor> lookingFor) {
        this.email = email;
        this.username = username;
        this.password = password;
        this.userRole = userRole;
        this.rank = rank;
        this.firstName = firstName;
        this.birth = birth;
        this.gender = gender;
        this.currentCity = currentCity;
        this.homeTown = homeTown;
        this.languagesISpeak = languagesISpeak;
        this.lookingFor = lookingFor;
        this.notifyAboutComments = true;
    }

    public UserResponseWithId mapToUserResponseWithId() {
        return new UserResponseWithId(
                this.userId,
                this.username,
                this.email,
                this.rank.name(),
                this.firstName,
                calculateAge(this.birth),
                this.currentCity,
                this.homeTown,
                this.languagesImLearning,
                this.languagesISpeak,
                this.lookingFor,
                this.education,
                this.relationshipStatus,
                this.aboutMe,
                this.followersAmount,
                this.followedAmount,
                this.notifyAboutComments,
                this.userRole);
    }

    private String calculateAge(LocalDate birthdate) {
        return String.valueOf(Period.between(birthdate, LocalDate.now()).getYears());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(userRole.name());
        return Collections.singletonList(authority);
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !isLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }
}
