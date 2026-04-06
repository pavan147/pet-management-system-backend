# Pet Community / Social Feature

This document explains the backend implementation for a mini pet forum in `pet-management-system-backend`.

## Goals

- Pet owners can share posts, photos, questions, and tips.
- Users can report lost pets and search lost-pet posts.
- Users can comment on posts.
- Every comment response includes commenter role.
- Admin can delete any post.
- Non-admin users can delete only their own posts.

## Supported Roles

1. `ROLE_ADMIN`
2. `ROLE_PET_OWNER`
3. `ROLE_RECEPTIONIST`
4. `ROLE_DOCTOR`

## Data Model

### `PetCommunityPost`

- `id`
- `title` (required)
- `content` (required)
- `category` (`GENERAL`, `QUESTION`, `TIP`, `LOST_PET`, `DISEASE_ALERT`)
- `location` (optional)
- `petName` (optional)
- `contactInfo` (optional)
- `photo` + `photoContentType` (optional)
- `author` (linked to `Owner`)
- `createdAt`, `updatedAt`

### `PostComment`

- `id`
- `comment` (required)
- `post` (linked to `PetCommunityPost`)
- `author` (linked to `Owner`)
- `createdAt`

## API Endpoints

Base path: `/api/community`

> All endpoints require JWT authentication.

### 1) Create post (with optional photo)

- `POST /posts`
- Content type: `multipart/form-data`
- Parts:
  - `post`: JSON string matching `CommunityPostRequestDTO`
  - `photo`: image file (optional)

Example `post` JSON:

```json
{
  "title": "Lost Labrador near MG Road",
  "content": "Brown labrador missing since yesterday evening.",
  "category": "LOST_PET",
  "location": "MG Road, Pune",
  "petName": "Bruno",
  "contactInfo": "+91-9XXXXXXXXX"
}
```

### 2) Feed with optional filters

- `GET /posts?category=TIP&keyword=vaccine&page=0&size=10`
- Filters:
  - `category` optional
  - `keyword` optional (searches title/content/location)
  - pagination: `page`, `size`

### 3) Lost pet feed

- `GET /posts/lost-pets?page=0&size=10`

### 4) Get single post with comments

- `GET /posts/{postId}`

### 5) Add comment (role included in response)

- `POST /posts/{postId}/comments`
- Body:

```json
{
  "comment": "I saw a similar dog near City Mall gate."
}
```

Response includes:

- `commenterName`
- `commenterRole`

### 6) Delete post

- `DELETE /posts/{postId}`

Authorization behavior:

- `ROLE_ADMIN`: can delete any post
- Other roles: can delete only own post

### 7) Get post photo

- `GET /posts/{postId}/photo`

## Authorization Rules

- Endpoint access: `ROLE_ADMIN`, `ROLE_PET_OWNER`, `ROLE_RECEPTIONIST`, `ROLE_DOCTOR`
- Service-level ownership check protects delete operation.

## Error Handling

- Validation errors return `400`.
- Missing post/photo returns `404`.
- Unauthorized delete returns `403`.
- Controlled API exceptions are returned using `TodoAPIException` status.

## Implementation Files

- `src/main/java/com/pet/manage/system/entity/PostCategory.java`
- `src/main/java/com/pet/manage/system/entity/PetCommunityPost.java`
- `src/main/java/com/pet/manage/system/entity/PostComment.java`
- `src/main/java/com/pet/manage/system/repository/PetCommunityPostRepository.java`
- `src/main/java/com/pet/manage/system/repository/PostCommentRepository.java`
- `src/main/java/com/pet/manage/system/dtos/request/CommunityPostRequestDTO.java`
- `src/main/java/com/pet/manage/system/dtos/request/PostCommentRequestDTO.java`
- `src/main/java/com/pet/manage/system/dtos/response/CommunityPostResponseDTO.java`
- `src/main/java/com/pet/manage/system/dtos/response/PostCommentResponseDTO.java`
- `src/main/java/com/pet/manage/system/service/CommunityService.java`
- `src/main/java/com/pet/manage/system/service/Impl/CommunityServiceImpl.java`
- `src/main/java/com/pet/manage/system/controller/CommunityController.java`
- `src/main/java/com/pet/manage/system/global/exception/GlobalExceptionHandler.java`
- `src/main/java/com/pet/manage/system/commons/Constants.java`

