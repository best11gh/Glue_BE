package org.glue.glue_be.post.repository;


import org.glue.glue_be.post.entity.Like;
import org.glue.glue_be.post.entity.Post;
import org.glue.glue_be.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {

	Optional<Like> findByUserAndPost(User user, Post post);

	List<Like> findByUser_UserIdOrderByCreatedAtDesc(Long userId);


	// 특정 유저가 여러 게시글에 좋아요를 눌렀는지 한 번에 조회
	// 쿼리문 읽을 때 개인적으로 from -> where순으로 읽고 select를 마지막에 읽으면 의미 이해가 좀 쉬웠음
	// Like 테이블에서, user_id가 입력 userId와 같고, post_id가 postIds 배열에 속해있는 것을 찾아, post_id 외래키 속성만 select
	@Query("SELECT l.post.id FROM Like l WHERE l.user.userId = :userId AND l.post.id IN :postIds")
	List<Long> findLikedPostIdsByUserAndPostIds(@Param("userId") Long userId, @Param("postIds") List<Long> postIds);

	Boolean existsByUser_UserIdAndPost_Id(Long userId, Long postId);

	void deleteByPost_Id(Long postId);

}
