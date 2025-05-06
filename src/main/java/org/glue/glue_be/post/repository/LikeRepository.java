package org.glue.glue_be.post.repository;


import org.glue.glue_be.post.entity.Like;
import org.glue.glue_be.post.entity.Post;
import org.glue.glue_be.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {

	Optional<Like> findByUserAndPost(User user, Post post);

}
