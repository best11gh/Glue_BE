package org.glue.glue_be.user.repository;

import org.glue.glue_be.user.entity.ProfileImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileImageRepository extends JpaRepository<ProfileImage, Long> {
    ProfileImage findByUser_UserId(Long userId);

}
