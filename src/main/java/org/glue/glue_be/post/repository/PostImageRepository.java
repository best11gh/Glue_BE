package org.glue.glue_be.post.repository;


import org.glue.glue_be.post.entity.PostImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface PostImageRepository extends JpaRepository<PostImage, Long> {

	List<PostImage> findAllByPost_Id(Long postId);

	void deleteByPost_Id(Long postId);

	void deleteByPost_IdAndImageUrl(Long postId, String url);

}
