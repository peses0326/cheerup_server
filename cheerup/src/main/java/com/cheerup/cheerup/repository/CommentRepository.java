package com.cheerup.cheerup.repository;

import com.cheerup.cheerup.model.Comment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findAllByOrderByIdDesc();
    Page<Comment> findAllByOrderByCreatedAtDesc(Pageable pageable);
    Optional<List<Comment>> findAllByArticleIdOrderByModifiedAtDesc(Long articleid);
    Long countByArticleId(Long articleId);
}