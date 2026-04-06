package com.pet.manage.system.dtos.request;

import com.pet.manage.system.entity.PostCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CommunityPostRequestDTO {

    @NotBlank(message = "Title is required")
    @Size(max = 120, message = "Title cannot be more than 120 characters")
    private String title;

    @NotBlank(message = "Content is required")
    @Size(max = 5000, message = "Content cannot be more than 5000 characters")
    private String content;

    @NotNull(message = "Category is required")
    private PostCategory category;

    @Size(max = 120, message = "Location cannot be more than 120 characters")
    private String location;

    @Size(max = 100, message = "Pet name cannot be more than 100 characters")
    private String petName;

    @Size(max = 80, message = "Contact info cannot be more than 80 characters")
    private String contactInfo;
}

