package pl.mmilewczyk.groupservice.model.entity;

import lombok.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import pl.mmilewczyk.clients.user.UserResponseWithId;
import pl.mmilewczyk.groupservice.model.dto.GroupInvitationRequest;
import pl.mmilewczyk.groupservice.model.dto.GroupResponse;
import pl.mmilewczyk.groupservice.model.enums.InvitationStatus;

import javax.persistence.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class GroupInvitation {

    @Id
    @SequenceGenerator(name = "group_invitation_id_sequence", sequenceName = "group_invitation_id_sequence")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "group_invitation_id_sequence")
    private Long groupInvitationId;

    private Long groupId;
    private Long inviterId;
    private Long inviteeId;
    private InvitationStatus status;

    public GroupInvitation(Long groupId, Long inviterId, Long inviteeId, InvitationStatus status) {
        this.groupId = groupId;
        this.inviterId = inviterId;
        this.inviteeId = inviteeId;
        this.status = status;
    }

    public GroupInvitationRequest mapGroupInvitationToGroupInvitationRequest(GroupResponse group, UserResponseWithId inviter, UserResponseWithId invitee) {
        if (group != null && inviter != null && invitee != null) {
            return new GroupInvitationRequest(this.getGroupId(), group.groupName(), inviter.userId(), inviter.username(), invitee.userId(), invitee.username());
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE);
        }
    }
}
