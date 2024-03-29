package pl.mmilewczyk.groupservice.model.entity;

import lombok.*;
import pl.mmilewczyk.clients.user.UserResponseWithId;
import pl.mmilewczyk.groupservice.model.dto.GroupResponseLite;

import javax.persistence.*;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Table(name="\"Group\"")
public class Group {

    @Id
    @SequenceGenerator(name = "group_id_sequence", sequenceName = "group_id_sequence")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "group_id_sequence")
    private Long groupId;

    private String groupName;
    private String description;

    @ElementCollection
    private List<Long> postsIds;

    private Long authorId;

    @ElementCollection
    private List<Long> moderatorsIds;

    @ElementCollection
    private List<Long> membersIds;

    @ElementCollection
    private List<Long> eventsIds;

    public boolean isComplete() {
        return groupName != null && authorId != null && description != null;
    }

    public GroupResponseLite mapGroupToGroupResponseLite() {
        return new GroupResponseLite(
                this.getGroupId(),
                this.getGroupName(),
                this.getDescription(),
                (long) this.getPostsIds().size(),
                (long) this.getMembersIds().size(),
                (long) this.getEventsIds().size());
    }

    public Group(String groupName, String description, Long authorId) {
        this.groupName = groupName;
        this.description = description;
        this.authorId = authorId;
    }

    public Group(String groupName, String description, List<Long> postsIds, Long authorId, List<Long> moderatorsIds, List<Long> membersIds, List<Long> eventsIds) {
        this.groupName = groupName;
        this.description = description;
        this.postsIds = postsIds;
        this.authorId = authorId;
        this.moderatorsIds = moderatorsIds;
        this.membersIds = membersIds;
        this.eventsIds = eventsIds;
    }

    public boolean isUserAMemberOfGroup(UserResponseWithId userResponseWithId) {
        return this.getMembersIds().contains(userResponseWithId.userId());
    }
}
