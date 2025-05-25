package org.glue.glue_be.post.repository;


import java.util.Optional;
import org.glue.glue_be.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;


@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

	@Query("SELECT p FROM Post p WHERE p.meeting.meetingId = :meetingId")
	Optional<Post> findByMeetingId(@Param("meetingId") Long meetingId);

	@Query(value = """
        SELECT  p.*
        FROM    post p
        JOIN    meeting m ON m.meeting_id = p.meeting_id
        ORDER BY
            COALESCE(p.bumped_at, m.created_at) DESC,
            p.post_id DESC
        LIMIT :limit
        """, nativeQuery = true)
	List<Post> fetchFirstPage(@Param("limit") int limit); // 카테고리 미지정 첫 스크롤 게시글


	 // 카테고리 미지정 이후 스크롤은 커서 기준 이후 게시글들을 가져온다
	 // cursorSortAt: 직전 페이지의 마지막 글의 sort 기준 시간(String type because of sqlite)
	 // cursorPostId: 직전 페이지의 마지막 글 pk
	@Query(value = """
        SELECT  p.*
        FROM    post p
        JOIN    meeting m ON m.meeting_id = p.meeting_id
        WHERE   (COALESCE(p.bumped_at, m.created_at) < :cursorSortAt)
            OR (COALESCE(p.bumped_at, m.created_at) = :cursorSortAt AND p.post_id < :cursorPostId)
        ORDER BY
            COALESCE(p.bumped_at, m.created_at) DESC,
            p.post_id DESC
        LIMIT :limit
        """, nativeQuery = true)
	List<Post> fetchNextPage(@Param("cursorSortAt") String cursorSortAt,
		@Param("cursorPostId") Long cursorPostId,
		@Param("limit") int limit);


    //  카테고리별 첫 페이지
    @Query(value = """
        SELECT  p.*
        FROM    post p
        JOIN    meeting m ON m.meeting_id = p.meeting_id
        WHERE   m.category_id = :categoryId
        ORDER BY
            COALESCE(p.bumped_at, m.created_at) DESC,
            p.post_id DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<Post> fetchFirstPageByCategory(@Param("categoryId") Integer categoryId, @Param("limit") int limit);


    // ── 카테고리별 이후 페이지
    @Query(value = """
        SELECT  p.*
        FROM    post p
        JOIN    meeting m ON m.meeting_id = p.meeting_id
        WHERE   m.category_id = :categoryId
          AND ((COALESCE(p.bumped_at, m.created_at) < :cursorSortAt)
               OR (COALESCE(p.bumped_at, m.created_at) = :cursorSortAt AND p.post_id < :cursorPostId))
        ORDER BY
            COALESCE(p.bumped_at, m.created_at) DESC,
            p.post_id DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<Post> fetchNextPageByCategory(@Param("categoryId") Integer categoryId,
                                       @Param("cursorSortAt") String cursorSortAt,
                                       @Param("cursorPostId") Long cursorPostId,
                                       @Param("limit") int limit);


	@Query("SELECT p FROM Post p WHERE p.meeting.host.userId = :hostUserId")
	List<Post> findByHostUserId(@Param("hostUserId") Long hostUserId);

	@Query("SELECT DISTINCT p FROM Post p " +
		"JOIN p.meeting.participants pt " +
		"WHERE pt.user.userId = :userId AND p.meeting.host.userId != :userId")
	List<Post> findByParticipantUserIdExcludingHost(@Param("userId") Long userId);

}
