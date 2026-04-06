package com.pet.manage.system.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pet.manage.system.dtos.request.CommunityPostRequestDTO;
import com.pet.manage.system.dtos.request.PostCommentRequestDTO;
import com.pet.manage.system.dtos.response.CommunityPostResponseDTO;
import com.pet.manage.system.dtos.response.PostCommentResponseDTO;
import com.pet.manage.system.entity.PostCategory;
import com.pet.manage.system.service.CommunityService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@RestController
@RequestMapping("/api/community")
@CrossOrigin(origins = "http://localhost:5173")
@AllArgsConstructor
public class CommunityController {

    private final CommunityService communityService;
    private final Validator validator;

    @PostMapping(value = "/posts", consumes = {"multipart/form-data"})
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_PET_OWNER','ROLE_RECEPTIONIST','ROLE_DOCTOR')")
    public ResponseEntity<CommunityPostResponseDTO> createPost(
            @RequestPart("post") String postRequest,
            @RequestPart(value = "photo", required = false) MultipartFile photo
    ) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        CommunityPostRequestDTO requestDTO = objectMapper.readValue(postRequest, CommunityPostRequestDTO.class);
        Set<ConstraintViolation<CommunityPostRequestDTO>> violations = validator.validate(requestDTO);
        if (!violations.isEmpty()) {
            String message = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .findFirst()
                    .orElse("Invalid post payload");
            throw new IllegalArgumentException(message);
        }
        return ResponseEntity.ok(communityService.createPost(requestDTO, photo));
    }

    @GetMapping("/posts")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_PET_OWNER','ROLE_RECEPTIONIST','ROLE_DOCTOR')")
    public ResponseEntity<Page<CommunityPostResponseDTO>> getFeed(
            @RequestParam(required = false) PostCategory category,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(communityService.getFeed(category, keyword, page, size));
    }

    @GetMapping("/posts/lost-pets")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_PET_OWNER','ROLE_RECEPTIONIST','ROLE_DOCTOR')")
    public ResponseEntity<Page<CommunityPostResponseDTO>> getLostPetFeed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(communityService.getLostPetFeed(page, size));
    }

    @GetMapping("/posts/{postId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_PET_OWNER','ROLE_RECEPTIONIST','ROLE_DOCTOR')")
    public ResponseEntity<CommunityPostResponseDTO> getPostById(@PathVariable Long postId) {
        return ResponseEntity.ok(communityService.getPostById(postId));
    }

    @PostMapping("/posts/{postId}/comments")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_PET_OWNER','ROLE_RECEPTIONIST','ROLE_DOCTOR')")
    public ResponseEntity<PostCommentResponseDTO> addComment(
            @PathVariable Long postId,
            @Valid @RequestBody PostCommentRequestDTO requestDTO
    ) {
        return ResponseEntity.ok(communityService.addComment(postId, requestDTO));
    }

    @DeleteMapping("/posts/{postId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_PET_OWNER','ROLE_RECEPTIONIST','ROLE_DOCTOR')")
    public ResponseEntity<Void> deletePost(@PathVariable Long postId) {
        communityService.deletePost(postId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/posts/{postId}/photo")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_PET_OWNER','ROLE_RECEPTIONIST','ROLE_DOCTOR')")
    public ResponseEntity<byte[]> getPostPhoto(@PathVariable Long postId) {
        byte[] photo = communityService.getPostPhoto(postId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline().filename("community_post_" + postId + "_photo").build().toString())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(photo);
    }
}

