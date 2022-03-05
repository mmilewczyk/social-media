package pl.mmilewczyk.postservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Component;
import pl.mmilewczyk.clients.user.UserResponseWithId;
import pl.mmilewczyk.postservice.model.dto.PostResponse;
import pl.mmilewczyk.postservice.model.entity.Post;

@Component
@Mapper(componentModel = "spring")
public interface PostMapper {

    @Mapping(target = "authorUsername", source = "user.username")
    PostResponse mapPostToPostResponse(Post post, UserResponseWithId user);
}
