package org.glue.glue_be.user.repository;


import org.glue.glue_be.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;


@Repository
public interface UserRepository extends JpaRepository<User, Long> {

	// 1. oauthID로 유저 찾기
	Optional<User> findByOauthId(String oauthId);

	// 2. 이메일로 유저찾기
	Optional<User> findByEmail(String email);
}