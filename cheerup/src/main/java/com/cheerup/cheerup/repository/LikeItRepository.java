package com.cheerup.cheerup.repository;

import com.cheerup.cheerup.model.LikeIt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LikeItRepository extends JpaRepository<LikeIt, Long> {
    List<LikeIt> findByUsername(String username);
    List<LikeIt> findAllByArticleId(Long articleId);
    Long countByArticleId(Long articleId);
    void deleteByUsernameAndArticleId(String username, Long articleId);
    LikeIt findByUsernameAndArticleId(String username, Long articleId);
}