package org.glue.glue_be.main.repository;

import org.glue.glue_be.main.entity.CarouselDeployVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface CarouselDeployVersionRepository extends JpaRepository<CarouselDeployVersion, Long> {

    Optional<CarouselDeployVersion> findByIsActiveTrue();

    Optional<CarouselDeployVersion> findByVersion(String version);

}