package org.glue.glue_be.main.repository;

import org.glue.glue_be.main.entity.MainCarousel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MainCarouselRepository extends JpaRepository<MainCarousel, Long> {

    List<MainCarousel> findByVersionOrderByDisplayOrder(String version);

    @Query("SELECT MAX(m.displayOrder) FROM MainCarousel m WHERE m.version = :version")
    Integer findMaxDisplayOrderByVersion(@Param("version") String version);

    // 모든 버전 목록 조회 (중복 제거)
    @Query("SELECT DISTINCT m.version FROM MainCarousel m ORDER BY m.version")
    List<String> findAllVersions();
}