package pl.mmilewczyk.userservice.model.entity;

import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Builder
@AllArgsConstructor
@Entity
public class Followship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
