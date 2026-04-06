package com.pet.manage.system.service.Impl;

import com.pet.manage.system.commons.Constants;
import com.pet.manage.system.dtos.request.CommunityPostRequestDTO;
import com.pet.manage.system.dtos.request.PostCommentRequestDTO;
import com.pet.manage.system.dtos.response.CommunityPostResponseDTO;
import com.pet.manage.system.dtos.response.PostCommentResponseDTO;
import com.pet.manage.system.entity.Owner;
import com.pet.manage.system.entity.PetCommunityPost;
import com.pet.manage.system.entity.PostCategory;
import com.pet.manage.system.entity.PostComment;
import com.pet.manage.system.entity.Role;
import com.pet.manage.system.global.exception.TodoAPIException;
import com.pet.manage.system.repository.OwnerRepository;
import com.pet.manage.system.repository.PetCommunityPostRepository;
import com.pet.manage.system.repository.PostCommentRepository;
import com.pet.manage.system.service.CommunityService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Service
@AllArgsConstructor
public class CommunityServiceImpl implements CommunityService {

    private final PetCommunityPostRepository postRepository;
    private final PostCommentRepository commentRepository;
    private final OwnerRepository ownerRepository;

    @Override
    @Transactional
    public CommunityPostResponseDTO createPost(CommunityPostRequestDTO requestDTO, MultipartFile photo) throws IOException {
        Owner currentUser = getCurrentOwner();

        PetCommunityPost post = new PetCommunityPost();
        post.setTitle(requestDTO.getTitle().trim());
        post.setContent(requestDTO.getContent().trim());
        post.setCategory(requestDTO.getCategory());
        post.setLocation(safeTrim(requestDTO.getLocation()));
        post.setPetName(safeTrim(requestDTO.getPetName()));
        post.setContactInfo(safeTrim(requestDTO.getContactInfo()));
        post.setAuthor(currentUser);

        if (photo != null && !photo.isEmpty()) {
            post.setPhoto(photo.getBytes());
            post.setPhotoContentType(photo.getContentType());
        }

        PetCommunityPost savedPost = postRepository.save(post);
        return mapPost(savedPost, true);
    }

    @Override
    public Page<CommunityPostResponseDTO> getFeed(PostCategory category, String keyword, int page, int size) {
        Pageable pageable = buildPageable(page, size);
        String safeKeyword = safeTrim(keyword);

        return postRepository.findFeed(category, safeKeyword, pageable)
                .map(post -> mapPost(post, false));
    }

    @Override
    public CommunityPostResponseDTO getPostById(Long postId) {
        PetCommunityPost post = findPostOrThrow(postId);
        return mapPost(post, true);
    }

    @Override
    public Page<CommunityPostResponseDTO> getLostPetFeed(int page, int size) {
        Pageable pageable = buildPageable(page, size);
        return postRepository.findByCategoryOrderByCreatedAtDesc(PostCategory.LOST_PET, pageable)
                .map(post -> mapPost(post, false));
    }

    @Override
    @Transactional
    public PostCommentResponseDTO addComment(Long postId, PostCommentRequestDTO requestDTO) {
        Owner currentUser = getCurrentOwner();
        PetCommunityPost post = findPostOrThrow(postId);

        PostComment comment = new PostComment();
        comment.setComment(requestDTO.getComment().trim());
        comment.setPost(post);
        comment.setAuthor(currentUser);

        PostComment savedComment = commentRepository.save(comment);
        return mapComment(savedComment);
    }

    @Override
    @Transactional
    public void deletePost(Long postId) {
        Owner currentUser = getCurrentOwner();
        PetCommunityPost post = findPostOrThrow(postId);

        boolean isAdmin = hasRole(currentUser, Constants.ADMIN_ROLE);
        boolean isOwnerOfPost = post.getAuthor().getId().equals(currentUser.getId());

        if (!isAdmin && !isOwnerOfPost) {
            throw new TodoAPIException(HttpStatus.FORBIDDEN,
                    "You are not authorized to delete this post. Only admin or post owner can delete.");
        }

        postRepository.delete(post);
    }

    @Override
    public byte[] getPostPhoto(Long postId) {
        PetCommunityPost post = findPostOrThrow(postId);
        if (post.getPhoto() == null || post.getPhoto().length == 0) {
            throw new TodoAPIException(HttpStatus.NOT_FOUND, "Photo not found for this post");
        }
        return post.getPhoto();
    }

    private PetCommunityPost findPostOrThrow(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new TodoAPIException(HttpStatus.NOT_FOUND, "Post not found with id: " + postId));
    }

    private Owner getCurrentOwner() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new TodoAPIException(HttpStatus.UNAUTHORIZED, "User is not authenticated");
        }

        return ownerRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new TodoAPIException(HttpStatus.UNAUTHORIZED, "Authenticated user not found"));
    }

    private Pageable buildPageable(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = size <= 0 ? 10 : Math.min(size, 50);
        return PageRequest.of(safePage, safeSize);
    }

    private boolean hasRole(Owner user, String roleName) {
        return user.getRoles().stream().map(Role::getName).anyMatch(roleName::equals);
    }

    private String safeTrim(String value) {
        return value == null ? null : value.trim();
    }

    private CommunityPostResponseDTO mapPost(PetCommunityPost post, boolean includeComments) {
        List<PostCommentResponseDTO> comments = includeComments
                ? commentRepository.findByPostIdOrderByCreatedAtAsc(post.getId()).stream().map(this::mapComment).toList()
                : List.of();

        return CommunityPostResponseDTO.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .category(post.getCategory())
                .location(post.getLocation())
                .petName(post.getPetName())
                .contactInfo(post.getContactInfo())
                .authorName(post.getAuthor().getOwnerName())
                .authorEmail(post.getAuthor().getEmail())
                .authorRole(primaryRole(post.getAuthor()))
                .hasPhoto(post.getPhoto() != null && post.getPhoto().length > 0)
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .commentCount(includeComments ? comments.size() : commentRepository.countByPostId(post.getId()))
                .comments(comments)
                .build();
    }

    private PostCommentResponseDTO mapComment(PostComment comment) {
        return PostCommentResponseDTO.builder()
                .id(comment.getId())
                .postId(comment.getPost().getId())
                .comment(comment.getComment())
                .commenterName(comment.getAuthor().getOwnerName())
                .commenterRole(primaryRole(comment.getAuthor()))
                .createdAt(comment.getCreatedAt())
                .build();
    }

    private String primaryRole(Owner owner) {
        Set<String> roleNames = owner.getRoles().stream().map(Role::getName).collect(java.util.stream.Collectors.toSet());
        return roleNames.stream()
                .sorted(Comparator.comparingInt(this::rolePriority))
                .findFirst()
                .orElse(Constants.PET_OWNER_ROLE);
    }

    private int rolePriority(String role) {
        if (Constants.ADMIN_ROLE.equals(role)) {
            return 0;
        }
        if (Constants.DOCTOR_ROLE.equals(role)) {
            return 1;
        }
        if (Constants.RECEPTIONIST_ROLE.equals(role)) {
            return 2;
        }
        if (Constants.PET_OWNER_ROLE.equals(role)) {
            return 3;
        }
        return 4;
    }
}

