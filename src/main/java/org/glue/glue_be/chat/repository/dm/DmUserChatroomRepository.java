package org.glue.glue_be.chat.repository.dm;

import org.glue.glue_be.chat.entity.dm.DmChatRoom;
import org.glue.glue_be.chat.entity.dm.DmUserChatroom;
import org.glue.glue_be.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DmUserChatroomRepository extends JpaRepository<DmUserChatroom, Long> {
    List<DmUserChatroom> findByDmChatRoom(DmChatRoom dmChatRoom);

    @Query("SELECT uc.user.userId FROM DmUserChatroom uc WHERE uc.dmChatRoom.id = :dmChatRoomId")
    List<Long> findUserIdsByDmChatRoomId(@Param("dmChatRoomId") Long dmChatRoomId);

    void deleteByDmChatRoomAndUser(DmChatRoom dmChatRoom, User user);

    Optional<DmUserChatroom> findByDmChatRoomAndUser(DmChatRoom dmChatRoom, User sender);
}
