package com.pet.manage.system.dtos.response;

import com.pet.manage.system.entity.PostCategory;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class CommunityPostResponseDTO {
    private Long id;
    private String title;
    private String content;
    private PostCategory category;
    private String location;
    private String petName;
    private String contactInfo;
    private String authorName;
    private String authorRole;
    private String authorEmail;
    private boolean hasPhoto;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private long commentCount;
    private List<PostCommentResponseDTO> comments;
}

