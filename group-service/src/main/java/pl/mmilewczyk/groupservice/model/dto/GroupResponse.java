package pl.mmilewczyk.groupservice.model.dto;

import pl.mmilewczyk.clients.post.PostResponse;
import pl.mmilewczyk.clients.user.UserResponseWithId;

import java.util.List;

public record GroupResponse(Long groupId,
                            String groupName,
                            String description,
                            List<PostResponse> posts,
                            UserResponseWithId author,
                            List<UserResponseWithId> moderators,
                            List<UserResponseWithId> members,
                            List<Long> events) {
}
