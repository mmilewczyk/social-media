package pl.mmilewczyk.userservice.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import pl.mmilewczyk.userservice.model.dto.UserResponse;
import pl.mmilewczyk.userservice.model.dto.UserResponseWithId;
import pl.mmilewczyk.userservice.model.enums.RankName;
import pl.mmilewczyk.userservice.model.enums.RoleName;

import javax.persistence.*;
import java.util.Collection;
import java.util.Collections;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Users")
public class User implements UserDetails {

    @Id
    @SequenceGenerator(name = "user_id_sequence", sequenceName = "user_id_sequence")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_id_sequence")
    private Long userId;

    private String email;
    private String username;
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

    public User(String email, String username, String password, RoleName userRole, RankName rank) {
        this.email = email;
        this.username = username;
        this.password = password;
        this.userRole = userRole;
        this.rank = rank;
    }

    public UserResponseWithId mapToUserResponseWithId() {
        return new UserResponseWithId(this.userId, this.username, this.email, this.rank.name());
    }

    public UserResponse mapToUserResponse() {
        return new UserResponse(this.username, this.rank.name());
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
