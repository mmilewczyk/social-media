package pl.mmilewczyk.groupservice.model.dto;

public record GroupResponseLite(Long groupId,
                                String groupName,
                                String description,
                                Long postsAmount,
                                Long membersAmount,
                                Long eventsAmount) {
}
