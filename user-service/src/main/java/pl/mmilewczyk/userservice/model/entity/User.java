package pl.mmilewczyk.userservice.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.mmilewczyk.userservice.model.dto.RankDTO;
import pl.mmilewczyk.userservice.model.dto.UserResponse;
import pl.mmilewczyk.userservice.model.dto.UserResponseWithId;

import javax.persistence.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Users")
public class User {

    @Id
    @SequenceGenerator(name = "user_id_sequence", sequenceName = "user_id_sequence")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_id_sequence")
    private Long userId;

    private String email;
    private String username;
    private String password;

    @Enumerated(EnumType.STRING)
    @ManyToOne
    @JoinColumn(name = "rank_id")
    private Rank rank;

    public UserResponseWithId mapToUserResponseWithId() {
        return new UserResponseWithId(this.userId, this.username, this.email, new RankDTO(this.rank.getRankName(), this.rank.getIcon()));
    }

    public UserResponse mapToUserResponse() {
        return new UserResponse(this.username, new RankDTO(this.rank.getRankName(), this.rank.getIcon()));
    }
}
