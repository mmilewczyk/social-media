package pl.mmilewczyk.chatservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.mmilewczyk.chatservice.model.entity.PrivateChatChannel;

import java.util.List;

public interface PrivateChatChannelRepository extends JpaRepository<PrivateChatChannel, Long> {

    @Query("FROM PrivateChatChannel c " +
            "WHERE c.firstUserId IN (:firstUserId, :secondUserId) " +
            "AND c.secondUserId IN (:firstUserId, :secondUserId)")
    List<PrivateChatChannel> findExistingChannel(
            @Param("firstUserId") Long firstUserId,
            @Param("secondUserId") Long secondUserId);
}
