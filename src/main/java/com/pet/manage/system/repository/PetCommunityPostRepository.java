package com.pet.manage.system.repository;

import com.pet.manage.system.entity.PetCommunityPost;
import com.pet.manage.system.entity.PostCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PetCommunityPostRepository extends JpaRepository<PetCommunityPost, Long> {

    Page<PetCommunityPost> findByCategoryOrderByCreatedAtDesc(PostCategory category, Pageable pageable);

    @Query("""
            SELECT p FROM PetCommunityPost p
            WHERE (:category IS NULL OR p.category = :category)
              AND (:keyword IS NULL
                   OR LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(CAST(p.content AS string)) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(COALESCE(p.location, '')) LIKE LOWER(CONCAT('%', :keyword, '%')))
            ORDER BY p.createdAt DESC
            """)
    Page<PetCommunityPost> findFeed(@Param("category") PostCategory category,
                                    @Param("keyword") String keyword,
                                    Pageable pageable);
}

