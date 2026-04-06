package com.pet.manage.system.service;

import com.pet.manage.system.dtos.request.CommunityPostRequestDTO;
import com.pet.manage.system.dtos.request.PostCommentRequestDTO;
import com.pet.manage.system.dtos.response.CommunityPostResponseDTO;
import com.pet.manage.system.dtos.response.PostCommentResponseDTO;
import com.pet.manage.system.entity.PostCategory;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface CommunityService {

    CommunityPostResponseDTO createPost(CommunityPostRequestDTO requestDTO, MultipartFile photo) throws IOException;

    Page<CommunityPostResponseDTO> getFeed(PostCategory category, String keyword, int page, int size);

    CommunityPostResponseDTO getPostById(Long postId);

    Page<CommunityPostResponseDTO> getLostPetFeed(int page, int size);

    PostCommentResponseDTO addComment(Long postId, PostCommentRequestDTO requestDTO);

    void deletePost(Long postId);

    byte[] getPostPhoto(Long postId);
}

