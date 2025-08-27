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
    private static final String POST_TYPE_FREE = "free";

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
            if (POST_TYPE_FREE.equalsIgnoreCase(type)) {
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

        // 1. CloudFront URL을 S3 URL로 변환 (필요한 경우)
        List<String> normalizedDeleteFiles = normalizeUrlsForComparison(deleteFiles);
        List<String> normalizedExisting = normalizeUrlsForComparison(existing);

        // 2. 삭제 대상 필터링 - 정규화된 URL로 비교
        List<String> matched = new ArrayList<>();
        for (int i = 0; i < existing.size(); i++) {
            if (normalizedDeleteFiles.contains(normalizedExisting.get(i))) {
                matched.add(existing.get(i)); // 원본 URL 사용
            }
        }

        // 3. 유효성 검증
        if (matched.size() != deleteFiles.size()) {
            throw new CustomException(PostErrorCode.FILE_NOT_FOUND);
        }

        // 4. S3 삭제 - 실제 S3 URL(matched)로 삭제
        uploadUtilAsyncWrapper.deleteImagesAsync(matched, UploadUtil.BucketType.MEDIA);

        // 5. 연관관계 해제
        existing.removeAll(matched);

// 6. Free post 썸네일 동기화
        if (post instanceof FreePost && post.getThumbnailUrls() != null) {
            List<String> thumbnailUrls = new ArrayList<>(post.getThumbnailUrls());

            // 삭제 대상 인덱스 추출
            List<Integer> indicesToDelete = new ArrayList<>();
            for (String deletedImage : matched) {
                int index = existing.indexOf(deletedImage); // existing == 원본 이미지 리스트
                if (index >= 0 && index < thumbnailUrls.size()) {
                    indicesToDelete.add(index);
                }
            }

            // 썸네일 삭제할 리스트 수집
            List<String> thumbnailsToDelete = new ArrayList<>();
            for (int idx : indicesToDelete) {
                thumbnailsToDelete.add(thumbnailUrls.get(idx));
            }

            // 인덱스 역순으로 정렬 후 삭제
            indicesToDelete.sort((a, b) -> Integer.compare(b, a));
            for (int idx : indicesToDelete) {
                thumbnailUrls.remove(idx);
            }

            // 썸네일 S3 삭제
            if (!thumbnailsToDelete.isEmpty()) {
                uploadUtilAsyncWrapper.deleteImagesAsync(thumbnailsToDelete, UploadUtil.BucketType.MEDIA);
            }

            // 모든 이미지가 삭제된 경우 썸네일도 null 처리
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
     * Free Post의 썸네일과 이미지 순서 동기화
     * 이미지 개수와 썸네일 개수가 다른 경우 썸네일을 null로 설정
     */
    private void synchronizeFreePostThumbnails(Post post) {
        if (!(post instanceof FreePost)) {
            return;
        }
        
        List<String> imageUrls = post.getImageUrls();
        List<String> thumbnailUrls = post.getThumbnailUrls();
        
        // 이미지와 썸네일 개수가 다르거나 썸네일이 null인 경우
        if (thumbnailUrls == null || imageUrls.size() != thumbnailUrls.size()) {
            // 썸네일을 null로 설정하여 불일치 방지
            post.setThumbnailUrls(null);
            
            // 필요시 기존 썸네일들을 S3에서 삭제 (선택적)
            if (thumbnailUrls != null && !thumbnailUrls.isEmpty()) {
                uploadUtilAsyncWrapper.deleteImagesAsync(thumbnailUrls, UploadUtil.BucketType.MEDIA);
            }
        }
    }

    /**
     * URL 비교를 위한 정규화 처리
     * CloudFront URL과 S3 URL을 모두 동일한 형태로 변환하여 비교 가능하게 함
     */
    private List<String> normalizeUrlsForComparison(List<String> urls) {
        return urls.stream()
                .map(this::normalizeUrlForComparison)
                .toList();
    }
    
    /**
     * 단일 URL 정규화 처리
     * URL에서 도메인 부분을 제거하고 경로만 추출하여 비교
     */
    private String normalizeUrlForComparison(String url) {
        if (url == null || url.isEmpty()) {
            return url;
        }
        
        // URL에서 경로 부분만 추출 (도메인 제거)
        // 예: https://cloudfront.amazonaws.com/path/to/file.jpg -> /path/to/file.jpg
        // 또는 https://s3.amazonaws.com/bucket/path/to/file.jpg -> /path/to/file.jpg
        try {
            if (url.startsWith("http://") || url.startsWith("https://")) {
                int thirdSlashIndex = url.indexOf('/', url.indexOf("://") + 3);
                if (thirdSlashIndex > 0) {
                    return url.substring(thirdSlashIndex);
                }
            }
            return url;
        } catch (Exception e) {
            // URL 파싱 실패 시 원본 반환
            return url;
        }
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
            if (POST_TYPE_FREE.equalsIgnoreCase(type)) {
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

        // Free post 썸네일 동기화 - 이미지와 썸네일 순서 일치 보장
        if (POST_TYPE_FREE.equalsIgnoreCase(type) && post.getThumbnailUrls() != null) {
            synchronizeFreePostThumbnails(post);
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