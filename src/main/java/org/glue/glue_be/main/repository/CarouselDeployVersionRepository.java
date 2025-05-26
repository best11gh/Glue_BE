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

    @Query("SELECT COUNT(c) > 0 FROM CarouselDeployVersion c WHERE c.isActive = true")
    boolean existsActiveVersion();

    @Modifying
    @Transactional
    @Query("UPDATE CarouselDeployVersion c SET c.isActive = false WHERE c.isActive = true")
    void deactivateAllVersions();

    @Modifying
    @Transactional
    @Query("UPDATE CarouselDeployVersion c SET c.isActive = false WHERE c.isActive = true AND c.version != :version")
    void deactivateAllVersionsExcept(@Param("version") String version);

    List<CarouselDeployVersion> findAllByOrderByUpdatedAtDesc();

    List<CarouselDeployVersion> findByIsActiveOrderByUpdatedAtDesc(Boolean isActive);

    boolean existsByVersion(String version);

    @Query("SELECT c FROM CarouselDeployVersion c WHERE c.version LIKE %:pattern% ORDER BY c.version")
    List<CarouselDeployVersion> findByVersionContaining(@Param("pattern") String pattern);
}