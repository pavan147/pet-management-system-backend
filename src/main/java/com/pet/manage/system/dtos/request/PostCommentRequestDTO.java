package com.pet.manage.system.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PostCommentRequestDTO {

    @NotBlank(message = "Comment is required")
    @Size(max = 1500, message = "Comment cannot be more than 1500 characters")
    private String comment;
}

