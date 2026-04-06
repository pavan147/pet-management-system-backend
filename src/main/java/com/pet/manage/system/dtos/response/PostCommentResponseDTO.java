package com.pet.manage.system.dtos.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PostCommentResponseDTO {
    private Long id;
    private Long postId;
    private String comment;
    private String commenterName;
    private String commenterRole;
    private LocalDateTime createdAt;
}

