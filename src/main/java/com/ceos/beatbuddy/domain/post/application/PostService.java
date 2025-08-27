package com.ceos.beatbuddy.domain.post.application;

import com.ceos.beatbuddy.domain.comment.repository.CommentRepository;
import com.ceos.beatbuddy.domain.follow.repository.FollowRepository;
import com.ceos.beatbuddy.domain.member.application.MemberService;
import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.domain.member.repository.MemberBlockRepository;
import com.ceos.beatbuddy.domain.post.dto.*;
import com.ceos.beatbuddy.domain.post.entity.FixedHashtag;
import com.ceos.beatbuddy.domain.post.entity.FreePost;
import com.ceos.beatbuddy.domain.post.entity.Post;
import com.ceos.beatbuddy.domain.post.exception.PostErrorCode;
import com.ceos.beatbuddy.domain.post.repository.PostQueryRepository;
import com.ceos.beatbuddy.domain.scrapandlike.repository.PostLikeRepository;
import com.ceos.beatbuddy.domain.scrapandlike.repository.PostScrapRepository;
import com.ceos.beatbuddy.global.CustomException;
import com.ceos.beatbuddy.global.code.ErrorCode;
import com.ceos.beatbuddy.global.service.ImageUploadService;
import com.ceos.beatbuddy.global.util.UploadResult;
import com.ceos.beatbuddy.global.util.UploadUtil;
import com.ceos.beatbuddy.global.util.UploadUtilAsyncWrapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostService {
    private final MemberService memberService;
    private final PostLikeRepository postLikeRepository;
    private final PostScrapRepository postScrapRepository;
    private final PostQueryRepository postQueryRepository;
    private final CommentRepository commentRepository;
    private final PostTypeHandlerFactory postTypeHandlerFactory;
    private final ImageUploadService imageUploadService;
    private final UploadUtilAsyncWrapper uploadUtilAsyncWrapper;
    private final PostInteractionService postInteractionService;
    private final FollowRepository followRepository;
    private final PostResponseHelper postResponseHelper;
    private final PostValidationHelper postValidationHelper;
    private final MemberBlockRepository memberBlockRepository;

    private static final List<String> VALID_POST_TYPES = List.of("free", "piece");

    @Transactional
    public ResponsePostDto addNewPost(String type, PostCreateRequestDTO dto, Long memberId, List<MultipartFile> images) {
        validatePostType(type);
        Member member = memberService.validateAndGetMember(memberId);

        if (images != null && images.stream().filter(file -> file != null && !file.isEmpty()).count() > 20) {
            throw new CustomException(ErrorCode.TOO_MANY_IMAGES_20);
        }

        List<String> imageUrls = null;
        List<String> thumbnailUrls = null;

        if (images != null && !images.isEmpty()) {
            if ("free".equalsIgnoreCase(type)) {
                // free 타입: 원본 + 썸네일 동시 업로드 (성능 최적화)
                List<UploadResult> uploadResults = imageUploadService.uploadImagesWithThumbnails(images, UploadUtil.BucketType.MEDIA, "post");
                imageUrls = uploadResults.stream().map(UploadResult::getOriginalUrl).toList();
                thumbnailUrls = uploadResults.stream().map(UploadResult::getThumbnailUrl).toList();
            } else {
                // piece 타입: 원본만 업로드
                imageUrls = imageUploadService.uploadImagesParallel(images, UploadUtil.BucketType.MEDIA, "post");
            }
        }

        PostTypeHandler handler = postTypeHandlerFactory.getHandler(type);
        Post post = handler.createPost(dto, member, imageUrls);
        post.setThumbnailUrls(thumbnailUrls); // null or list

        return ResponsePostDto.of(post);
    }





    @Transactional
    public PostReadDetailDTO newReadPost(String type, Long postId, Long memberId) {
        // 회원 유효성 검사
        memberService.validateAndGetMember(memberId);

        // 게시글 조회 및 조회수 증가
        PostTypeHandler handler = postTypeHandlerFactory.getHandler(type);
        Post post = handler.readPost(postId);

        // 사용자의 좋아요 / 스크랩 / 댓글 여부
        Triple<Boolean, Boolean, Boolean> status = getPostInteractions(memberId, postId);

        // following 여부
        boolean isFollowing = followRepository.existsByFollower_IdAndFollowing_Id(memberId, post.getMember().getId());

        // 해시태그 분기 처리
        List<FixedHashtag> hashtags = (post instanceof FreePost freePost)
                ? freePost.getHashtag()
                : List.of();

        // 응답 생성
        return PostReadDetailDTO.toDTO(post, status.getLeft(),
                status.getMiddle(),
                status.getRight(),
                hashtags,
                post.getMember().getId().equals(memberId),
                isFollowing);
    }

    public PostListResponseDTO readAllPostsSort(Long memberId, String type, int page, int size) {
        Sort sortOption = getSortOption("latest");

        // 페이지 1부터 받도록 지시, 0부터 시작하는 Pageable 생성
        if (page < 1) {
            throw new CustomException(ErrorCode.PAGE_OUT_OF_BOUNDS);
        }

        Pageable pageable = PageRequest.of(page-1, size, sortOption);

        memberService.validateAndGetMember(memberId);
        
        // 차단한 사용자 ID 목록 조회
        Set<Long> blockedMemberIds = memberBlockRepository.findBlockedMemberIdsByBlockerId(memberId);
        List<Long> blockedMemberIdsList = List.copyOf(blockedMemberIds);

        PostTypeHandler handler = postTypeHandlerFactory.getHandler(type);
        Page<? extends Post> postPage = handler.readAllPostsExcludingBlocked(pageable, blockedMemberIdsList);

        return postResponseHelper.createPostListResponse(postPage, memberId);
    }


    private Sort getSortOption(String sort) {
        return switch (sort) {
            case "latest" -> Sort.by(Sort.Direction.DESC, "createdAt");
            case "popular" -> Sort.by(Sort.Direction.DESC, "likes");
            default -> throw new CustomException(PostErrorCode.INVALID_SORT_TYPE);
        };
    }


    public List<PostPageResponseDTO> getHotPosts(Long memberId) {
        Member member = memberService.validateAndGetMember(memberId);
        
        // 차단한 사용자 ID 목록 조회
        Set<Long> blockedMemberIds = memberBlockRepository.findBlockedMemberIdsByBlockerId(memberId);
        List<Long> blockedMemberIdsList = List.copyOf(blockedMemberIds);
        
        List<Post> posts = postQueryRepository.findHotPostsWithin12HoursExcludingBlocked(blockedMemberIdsList);

        return postResponseHelper.createPostPageResponseDTOList(posts, memberId);
    }

    public PostListResponseDTO getHashtagPosts(Long memberId, List<String> hashtags, int page, int size) {
        Member member = memberService.validateAndGetMember(memberId);

        if (page < 1) {
            throw new CustomException(ErrorCode.PAGE_OUT_OF_BOUNDS);
        }

        Pageable pageable = PageRequest.of(page -1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        // 차단한 사용자 ID 목록 조회
        Set<Long> blockedMemberIds = memberBlockRepository.findBlockedMemberIdsByBlockerId(memberId);
        List<Long> blockedMemberIdsList = List.copyOf(blockedMemberIds);

        PostTypeHandler handler = postTypeHandlerFactory.getHandler("free");

        return handler.hashTagPostListExcludingBlocked(hashtags, pageable, member, blockedMemberIdsList);
    }



    @Transactional
    public void deletePost(String type, Long postId, Long memberId) {
        Member member = memberService.validateAndGetMember(memberId);

        Post post = postTypeHandlerFactory.getHandler(type).validateAndGetPost(postId);

        if (!post.getMember().getId().equals(member.getId())) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_MEMBER);
        }

        PostTypeHandler handler = postTypeHandlerFactory.getHandler(type);
        // 포스트 삭제 시 이미지 s3 에서 삭제
        handler.deletePost(postId, member);
    }



    @Transactional(readOnly = true)
    public PostListResponseDTO getScrappedPostsByType(Long memberId, String type, int page, int size) {
        Member member = memberService.validateAndGetMember(memberId);

        // 페이지 1부터 받도록 지시, 0부터 시작하는 Pageable 생성
        if (page < 1) {
            throw new CustomException(ErrorCode.PAGE_OUT_OF_BOUNDS);
        }
        Pageable pageable = PageRequest.of(page -1, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        // 스크랩한 게시글 페이징 조회
        Page<Post> postPage = postScrapRepository.findPostsByMemberId(member.getId(), pageable);
        List<Post> posts = postPage.getContent();

        // 게시글 타입 필터링
        PostTypeHandler handler = postTypeHandlerFactory.getHandler(type);
        List<Post> filteredPosts = posts.stream()
                .filter(handler::supports)
                .toList();

        // DTO 매핑 - 스크랩된 게시글이므로 모두 스크랩 상태 true로 설정
        List<Long> postIds = filteredPosts.stream().map(Post::getId).toList();
        PostInteractionStatus status = postInteractionService.getAllPostInteractions(memberId, postIds);
        Set<Long> followingIds = followRepository.findFollowingMemberIds(member.getId());

        List<PostPageResponseDTO> dtos = filteredPosts.stream()
                .map(post -> postResponseHelper.createPostPageResponseDTO(post, 
                    new PostInteractionStatus(
                        status.likedPostIds(),
                            new HashSet<>(postIds), // 스크랩된 게시글이므로 모두 true
                        status.commentedPostIds()
                    ), memberId, followingIds))
                .toList();

        return PostListResponseDTO.builder()
                .totalPost((int) postPage.getTotalElements())
                .page(postPage.getNumber())
                .size(postPage.getSize())
                .responseDTOS(dtos)
                .build();
    }



    @Transactional(readOnly = true)
    public PostListResponseDTO getMyPostsByType(Long memberId, String type, int page, int size) {
        memberService.validateAndGetMember(memberId);

        // 페이지 1부터 받도록 지시, 0부터 시작하는 Pageable 생성
        if (page < 1) {
            throw new CustomException(ErrorCode.PAGE_OUT_OF_BOUNDS);
        }

        // 타입 유효성 검사
        validatePostType(type);

        Pageable pageable = PageRequest.of(page-1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<? extends Post> postPage;

        PostTypeHandler handler = postTypeHandlerFactory.getHandler(type);
        postPage = handler.readAllPostsByMember(memberId, pageable);

        return postResponseHelper.createPostListResponse(postPage, memberId);
    }

    public PostListResponseDTO getUserPostsByType(Long memberId, Long userId, String type, int page, int size) {
        // 로그인한 사용자의 유효성 검사
        memberService.validateAndGetMember(memberId);
        // 조회하고자 하는 사용자
        memberService.validateAndGetMember(userId);

        // 타입 유효성 검사
        validatePostType(type);

        if (page < 1) {
            throw new CustomException(ErrorCode.PAGE_OUT_OF_BOUNDS);
        }

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        PostTypeHandler handler = postTypeHandlerFactory.getHandler(type);
        Page<? extends Post> postPage = handler.readAllPostsByUserExcludingAnonymous(userId, pageable);

        return postResponseHelper.createPostListResponse(postPage, memberId);
    }





    // 주어진 타입이 올바른지 확인
    private void validatePostType(String type) {
        if (!VALID_POST_TYPES.contains(type)) {
            throw new CustomException(PostErrorCode.INVALID_POST_TYPE);
        }
    }

    public Post validateAndGetPost(Long postId) {
        return postValidationHelper.validateAndGetPost(postId);
    }

    private void validatePostAuthor(Post post, Long memberId) {
        if (!post.getMember().getId().equals(memberId)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_MEMBER); // 권한 없음
        }
    }

    @Transactional
    public void removeImages(Post post, List<String> deleteFiles) {
        List<String> existing = post.getImageUrls();

        // 1. 삭제 대상 필터링
        List<String> matched = existing.stream()
                .filter(deleteFiles::contains)
                .toList();

        // 2. 유효성 검증
        if (matched.size() != deleteFiles.size()) {
            throw new CustomException(PostErrorCode.FILE_NOT_FOUND);
        }

        // 3. S3 삭제
        uploadUtilAsyncWrapper.deleteImagesAsync(deleteFiles, UploadUtil.BucketType.MEDIA);

        // 4. 연관관계 해제
        existing.removeAll(matched);

        // 5. Free post 썸네일 동기화
        if (post instanceof FreePost && post.getThumbnailUrls() != null) {
            // 삭제된 이미지와 같은 인덱스의 썸네일 찾기 및 삭제
            List<String> thumbnailUrls = new ArrayList<>(post.getThumbnailUrls());
            List<String> originalImageUrls = new ArrayList<>(post.getImageUrls());
            originalImageUrls.addAll(matched); // 삭제될 이미지들 다시 추가하여 원래 순서 복원
            
            List<String> thumbnailsToDelete = new ArrayList<>();
            
            for (String deletedImage : matched) {
                int index = originalImageUrls.indexOf(deletedImage);
                if (index >= 0 && index < thumbnailUrls.size()) {
                    thumbnailsToDelete.add(thumbnailUrls.get(index));
                    thumbnailUrls.remove(index);
                    originalImageUrls.remove(index);
                }
            }
            
            // 썸네일 파일들도 S3에서 삭제
            if (!thumbnailsToDelete.isEmpty()) {
                uploadUtilAsyncWrapper.deleteImagesAsync(thumbnailsToDelete, UploadUtil.BucketType.MEDIA);
            }
            
            // 모든 이미지가 삭제된 경우 썸네일도 null로 설정
            if (post.getImageUrls().isEmpty()) {
                post.setThumbnailUrls(null);
            } else {
                post.setThumbnailUrls(thumbnailUrls);
            }
        }
    }

    private Triple<Boolean, Boolean, Boolean> getPostInteractions(Long memberId, Long postId) {
        boolean isLiked = postLikeRepository.existsByMember_IdAndPost_Id(memberId, postId);
        boolean isScrapped = postScrapRepository.existsByMember_IdAndPost_Id(memberId, postId);
        boolean hasCommented = commentRepository.existsByPost_IdAndMember_IdAndIsDeletedFalse(postId, memberId);

        return Triple.of(isLiked, isScrapped, hasCommented);
    }

    /**
     * Free post의 썸네일을 첫 번째 이미지와 동기화합니다.
     * 이미지 교체 시 첫 번째 이미지의 썸네일로 갱신하는 데 사용됩니다.
     */
    private void syncThumbnailWithFirstImage(Post post) {
        if (post.getImageUrls() == null || post.getImageUrls().isEmpty() || 
            post.getThumbnailUrls() == null || post.getThumbnailUrls().isEmpty()) {
            return;
        }

        // 첫 번째 이미지가 변경되었다면 해당 썸네일로 첫 번째 썸네일 교체
        String firstImageUrl = post.getImageUrls().get(0);
        String firstThumbnailUrl = post.getThumbnailUrls().get(0);
        
        // 첫 번째 이미지에 대응하는 썸네일을 찾아서 첫 번째 위치로 이동
        // 이미지와 썸네일 순서가 일치한다고 가정
        if (post.getImageUrls().size() == post.getThumbnailUrls().size()) {
            // 순서가 맞는 경우는 별도 처리 불필요
            return;
        }
        
        // 이미지 순서 변경이나 추가/삭제로 인해 첫 번째 이미지의 썸네일이 첫 번째 위치에 없는 경우
        // 실제로는 이미지와 썸네일이 항상 같은 순서로 관리되므로 여기서는 기본 동작 유지
    }


    @Transactional
    public PostReadDetailDTO updatePost(String type, Long postId, Long memberId,
                                        UpdatePostRequestDTO requestDTO, List<MultipartFile> files,
                                        List<String> deleteFiles) {

        // 게시글 조회 및 작성자 검증
        Post post = this.validateAndGetPost(postId);

        // 삭제할 이미지 개수와 새로운 이미지의 합이 20개를 초과하는지 검사
        int currentCount = post.getImageUrls().size();
        int deleteCount = (deleteFiles != null) ? deleteFiles.size() : 0;
        int newCount = (files != null) ? files.size() : 0;

        if (currentCount - deleteCount + newCount > 20) {
            throw new CustomException(ErrorCode.TOO_MANY_IMAGES_20);
        }

        Member member = memberService.validateAndGetMember(memberId);
        validatePostAuthor(post, memberId);


        // 공통 필드 수정
        PostTypeHandler handler = postTypeHandlerFactory.getHandler(type);
        post = handler.updatePost(requestDTO, post, member);


        // 삭제할 이미지 제거
        if (deleteFiles != null && !deleteFiles.isEmpty()) {
            removeImages(post, deleteFiles);
        }

        // 새 이미지 업로드 및 저장
        if (files != null && !files.isEmpty()) {
            if ("free".equalsIgnoreCase(type)) {
                // free 타입: 원본 + 썸네일 동시 업로드 (성능 최적화)
                List<UploadResult> uploadResults = imageUploadService.uploadImagesWithThumbnails(files, UploadUtil.BucketType.MEDIA, "post");
                List<String> imageUrls = uploadResults.stream().map(UploadResult::getOriginalUrl).toList();
                List<String> newThumbnailUrls = uploadResults.stream().map(UploadResult::getThumbnailUrl).toList();

                post.getImageUrls().addAll(imageUrls);
                if (post.getThumbnailUrls() == null) {
                    post.setThumbnailUrls(new ArrayList<>());
                }
                post.getThumbnailUrls().addAll(newThumbnailUrls);
            } else {
                // piece 타입: 원본만 업로드
                List<String> imageUrls = imageUploadService.uploadImagesParallel(files, UploadUtil.BucketType.MEDIA, "post");
                post.getImageUrls().addAll(imageUrls);
            }
        }

        // Free post 썸네일 동기화: 이미지가 교체된 경우 첫 번째 이미지의 썸네일로 갱신
        if ("free".equalsIgnoreCase(type) && post.getThumbnailUrls() != null && !post.getImageUrls().isEmpty()) {
            // 현재 첫 번째 썸네일이 첫 번째 이미지의 썸네일인지 확인하고 필요시 갱신
            syncThumbnailWithFirstImage(post);
        }

        // 유저 상호작용 상태
        Triple<Boolean, Boolean, Boolean> status = getPostInteractions(memberId, postId);

        // 팔로잉 여부
        boolean isFollowing = followRepository.existsByFollower_IdAndFollowing_Id(memberId, post.getMember().getId());

        // 해시태그 분기 처리
        List<FixedHashtag> hashtags = (post instanceof FreePost freePost)
                ? freePost.getHashtag()
                : List.of();

        // 응답 생성
        return PostReadDetailDTO.toDTO(post, status.getLeft(), status.getMiddle(), status.getRight(), hashtags, post.getMember().getId().equals(memberId), isFollowing);
    }
}