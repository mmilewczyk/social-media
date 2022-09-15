package pl.mmilewczyk.userservice.model.entity;

import lombok.*;

import javax.persistence.*;

import static javax.persistence.GenerationType.IDENTITY;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Builder
@AllArgsConstructor
@Entity
public class Followship {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "following_user_user_id")
    private User followingUser;

    @ManyToOne
    @JoinColumn(name = "followed_user_user_id")
    private User followedUser;

    public Followship(User followingUser, User followedUser) {
        this.followingUser = followingUser;
        this.followedUser = followedUser;
    }
}
