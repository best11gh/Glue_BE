package org.glue.glue_be.chat.repository.dm;

import org.glue.glue_be.chat.entity.dm.DmChatRoom;
import org.glue.glue_be.chat.entity.dm.DmUserChatroom;
import org.glue.glue_be.user.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DmUserChatroomRepository extends JpaRepository<DmUserChatroom, Long> {
    List<DmUserChatroom> findByDmChatRoom(DmChatRoom dmChatRoom);

    void deleteByDmChatRoomAndUser(DmChatRoom dmChatRoom, User user);

    Optional<DmUserChatroom> findByDmChatRoomAndUser(DmChatRoom dmChatRoom, User sender);

    @Query("SELECT ducr.dmChatRoom FROM DmUserChatroom ducr WHERE ducr.user.userId = :userId")
    List<DmChatRoom> findDmChatRoomsByUserId(@Param("userId") Long userId);

    @Query("SELECT duc.dmChatRoom FROM DmUserChatroom duc " +
            "WHERE duc.user = :user " +
            "ORDER BY duc.dmChatRoom.id DESC")
    List<DmChatRoom> findDmChatRoomsByUserOrderByDmChatRoomIdDesc(
            @Param("user") User user,
            Pageable pageable);

    @Query("SELECT duc.dmChatRoom FROM DmUserChatroom duc " +
            "WHERE duc.user = :user " +
            "AND duc.dmChatRoom.id < :cursorId " +
            "ORDER BY duc.dmChatRoom.id DESC")
    List<DmChatRoom> findDmChatRoomsByUserAndDmChatRoomIdLessThanOrderByDmChatRoomIdDesc(
            @Param("user") User user,
            @Param("cursorId") Long cursorId,
            Pageable pageable);

	void deleteByDmChatRoom_Id(Long dmChatRoomId);

}