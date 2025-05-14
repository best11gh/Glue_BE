package org.glue.glue_be.post.repository;


import org.glue.glue_be.post.entity.PostImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface PostImageRepository extends JpaRepository<PostImage, Long> {

}
