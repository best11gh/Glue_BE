package org.glue.glue_be.post.repository;


import org.glue.glue_be.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;


@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

	//
	@Query(value = """
        SELECT  p.*
        FROM    post p
        JOIN    meeting m ON m.meeting_id = p.meeting_id
        ORDER BY
            COALESCE(p.bumped_at, m.created_at) DESC,
            p.post_id DESC
        LIMIT :limit
        """, nativeQuery = true)
	List<Post> fetchFirstPage(@Param("limit") int limit);

	/**
	 * 이후 페이지
	 *
	 * cursorSortAt  : 직전 페이지의 마지막 글의 sort 기준 시간(Sqlite는 date 관련 타입 미지원이라 string으로 받아야함!!!)
	 * cursorPostId  : 직전 페이지의 마지막 글의 ID
	 */
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
}
