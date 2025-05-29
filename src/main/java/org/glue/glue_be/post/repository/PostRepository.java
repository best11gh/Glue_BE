package org.glue.glue_be.post.repository;


import java.util.Optional;
import org.glue.glue_be.post.entity.Post;
import org.springframework.data.domain.Pageable;
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


	// 1) 검색어 기반 첫 페이지 조회
	@Query(value = """
        SELECT p.*
        FROM   post p
        JOIN   meeting m ON m.meeting_id = p.meeting_id
        WHERE  (p.post_title LIKE :kw OR p.content LIKE :kw)
        ORDER BY
          COALESCE(p.bumped_at, m.created_at) DESC,
          p.post_id DESC
        LIMIT :limit
        """, nativeQuery = true)
	List<Post> fetchFirstPageByKeyword(
		@Param("kw") String kw,
		@Param("limit") int limit
	);

	// 2) 검색어 + 커서 기반 다음 페이지 조회
	@Query(value = """
        SELECT p.*
        FROM   post p
        JOIN   meeting m ON m.meeting_id = p.meeting_id
        WHERE  
          (p.post_title LIKE :kw OR p.content LIKE :kw)
          AND (
            COALESCE(p.bumped_at, m.created_at) < :cursorSortAt
            OR (
              COALESCE(p.bumped_at, m.created_at) = :cursorSortAt
              AND p.post_id < :lastPostId
            )
          )
        ORDER BY
          COALESCE(p.bumped_at, m.created_at) DESC,
          p.post_id DESC
        LIMIT :limit
        """, nativeQuery = true)
	List<Post> fetchNextPageByKeyword(
		@Param("kw")           String kw,
		@Param("cursorSortAt") String cursorSortAt,
		@Param("lastPostId")   Long lastPostId,
		@Param("limit")        int limit
	);



	// 미팅시간 안넘긴 게시글 중 좋아요 상위 순 가져오기
	// Pageable 인자로 받지만 고정 사이즈의 게시글만 받아오므로 List<Post>로 리턴타입 지정
	@Query("""
        SELECT p
        FROM Post p
        WHERE p.meeting.meetingTime > :now
        ORDER BY SIZE(p.likes) DESC
        """)
	List<Post> findPopularPosts(
		@Param("now") LocalDateTime now,
		Pageable pageable
	);


	// 미팅시간 안넘긴 게시글 중 입력으로 들어온 주언어, 학습언어와 일치하는 게시글 가져오기
	// Pageable 인자로 받지만 고정 사이즈의 게시글만 받아오므로 List<Post>로 리턴타입 지정
	@Query("""
        SELECT p
        FROM Post p
        WHERE p.meeting.meetingMainLanguageId = :mainLang
          AND p.meeting.meetingExchangeLanguageId = :exchangeLang
          AND p.meeting.meetingTime > :now
        ORDER BY
          COALESCE(p.bumpedAt, p.meeting.createdAt) DESC,
          p.id DESC
        """)
	List<Post> findByLanguageMatch(
		@Param("mainLang") Integer mainLang,
		@Param("exchangeLang") Integer exchangeLang,
		@Param("now") LocalDateTime now,
		Pageable pageable
	);
}

