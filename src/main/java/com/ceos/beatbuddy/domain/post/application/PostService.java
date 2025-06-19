package com.ceos.beatbuddy.domain.post.application;

import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.domain.member.repository.MemberRepository;
import com.ceos.beatbuddy.domain.post.dto.*;
import com.ceos.beatbuddy.domain.post.dto.PostRequestDto.PiecePostRequestDto;
import com.ceos.beatbuddy.domain.post.entity.FreePost;
import com.ceos.beatbuddy.domain.post.entity.Piece;
import com.ceos.beatbuddy.domain.post.entity.PiecePost;
import com.ceos.beatbuddy.domain.post.entity.Post;
import com.ceos.beatbuddy.domain.post.exception.PostErrorCode;
import com.ceos.beatbuddy.domain.post.repository.FreePostRepository;
import com.ceos.beatbuddy.domain.post.repository.PiecePostRepository;
import com.ceos.beatbuddy.domain.post.repository.PieceRepository;
import com.ceos.beatbuddy.domain.post.repository.PostRepository;
import com.ceos.beatbuddy.domain.scrapandlike.entity.PostInteractionId;
import com.ceos.beatbuddy.domain.scrapandlike.entity.PostLike;
import com.ceos.beatbuddy.domain.scrapandlike.entity.PostScrap;
import com.ceos.beatbuddy.domain.scrapandlike.repository.PostLikeRepository;
import com.ceos.beatbuddy.domain.scrapandlike.repository.PostScrapRepository;
import com.ceos.beatbuddy.domain.venue.entity.Venue;
import com.ceos.beatbuddy.domain.venue.exception.VenueErrorCode;
import com.ceos.beatbuddy.domain.venue.repository.VenueRepository;
import com.ceos.beatbuddy.global.CustomException;
import com.ceos.beatbuddy.global.UploadUtil;
import com.ceos.beatbuddy.global.code.ErrorCode;
import com.ceos.beatbuddy.global.config.jwt.SecurityUtils;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostService {
    private final FreePostRepository freePostRepository;
    private final PiecePostRepository piecePostRepository;
    private final MemberRepository memberRepository;
    private final VenueRepository venueRepository;
    private final PieceRepository pieceRepository;
    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final PostScrapRepository postScrapRepository;

    @Autowired
    private UploadUtil uploadUtil;

    /**
     * 주어진 타입과 요청 정보를 기반으로 게시글을 생성합니다.
     *
     * @param type 생성할 게시글의 타입("free" 또는 "piece")
     * @param requestDto 게시글 생성에 필요한 데이터와 이미지 목록을 포함한 DTO
     * @return 생성된 게시글 엔티티
     * @throws CustomException 회원이 존재하지 않거나 게시글 타입이 유효하지 않은 경우 발생
     */
    @Transactional
    public Post addPost(String type, PostRequestDto requestDto) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(PostErrorCode.MEMBER_NOT_EXIST));

        List<String> imageUrls = uploadImages(requestDto.images());

        return switch (type) {
            case "free" -> createFreePost(member, requestDto, imageUrls);
            case "piece" -> createPiecePost(member, requestDto, imageUrls);
            default -> throw new CustomException(PostErrorCode.INVALID_POST_TYPE);
        };
    }

    /**
     * 주어진 타입과 데이터로 새로운 게시글을 생성하고 저장합니다.
     *
     * @param type 게시글 유형("free" 또는 "piece")
     * @param dto 게시글 생성 요청 데이터
     * @param memberId 게시글 작성자 ID
     * @param images 업로드할 이미지 파일 목록
     * @return 생성된 게시글의 응답 DTO
     * @throws CustomException 존재하지 않는 회원, 잘못된 게시글 유형, 또는 존재하지 않는 공연장(venue)일 경우 발생
     */
    @Transactional
    public ResponsePostDto addNewPost(String type, PostCreateRequestDTO dto, Long memberId, List<MultipartFile> images) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(PostErrorCode.MEMBER_NOT_EXIST));


        // 이미지 s3 올리기
        List<String> imageUrls = images.stream().map(( image -> {
            try {
                return uploadUtil.upload(image, UploadUtil.BucketType.MEDIA, "post");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        })).toList();

        Post savedPost = null;

        switch (type) {
            case "free" -> {
                Venue venue = dto.getVenueId() != null ?
                        venueRepository.findById(dto.getVenueId()).orElseThrow(
                                () -> new CustomException(VenueErrorCode.VENUE_NOT_EXIST)) :
                        null;

                FreePost freePost = new FreePost(dto.getHashtag(), imageUrls, dto.getTitle(), dto.getContent(), dto.getAnonymous(), member, venue);
                savedPost = freePostRepository.save(freePost);
            }

            case "piece" -> {
                // 아직 기능이 존재하지 않음.

            }

            default -> throw new CustomException(PostErrorCode.INVALID_POST_TYPE);
        }

        return ResponsePostDto.of(Objects.requireNonNull(savedPost));
    }

    /**
     * 지정된 타입과 ID에 해당하는 게시글을 조회하고 조회수를 1 증가시킵니다.
     *
     * @param type   게시글 타입 ("free" 또는 "piece")
     * @param postId 조회할 게시글의 ID
     * @return 조회된 게시글 엔티티
     * @throws CustomException 게시글이 존재하지 않거나 타입이 유효하지 않은 경우 발생
     */
    public Post readPost(String type, Long postId) {
        switch (type) {
            case "free" -> {
                FreePost post = freePostRepository.findById(postId)
                        .orElseThrow(() -> new CustomException(PostErrorCode.POST_NOT_EXIST));
                post.increaseView();  // 예: 조회수 증가
                return post;
            }
            case "piece" -> {
                PiecePost post = piecePostRepository.findById(postId)
                        .orElseThrow(() -> new CustomException(PostErrorCode.POST_NOT_EXIST));
                post.increaseView();  // 예: 조회수 증가
                return post;
            }
            default -> throw new CustomException(PostErrorCode.INVALID_POST_TYPE);
        }
    }

    public Page<ResponsePostDto> readAllPosts(String type, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return switch (type) {
            case "free" -> freePostRepository.findAll(pageable).map(ResponsePostDto::of);
            case "piece" -> piecePostRepository.findAll(pageable).map(ResponsePostDto::of);
            default -> throw new CustomException(PostErrorCode.INVALID_POST_TYPE);
        };
    }

    public PostListResponseDTO readAllPostsSort(String type, String sort, int page, int size) {
        Sort sortOption = getSortOption(sort);
        Pageable pageable = PageRequest.of(page, size, sortOption);

        Page<PostPageResponseDTO> resultPage = switch (type) {
            case "free" -> freePostRepository.findAll(pageable)
                    .map(PostPageResponseDTO::toDTO);
            case "piece" -> piecePostRepository.findAll(pageable)
                    .map(PostPageResponseDTO::toDTO);
            default -> throw new CustomException(PostErrorCode.INVALID_POST_TYPE);
        };

        return PostListResponseDTO.builder()
                .totalPost((int) resultPage.getTotalElements())
                .page(resultPage.getNumber())
                .size(resultPage.getSize())
                .responseDTOS(resultPage.getContent())
                .build();
    }

    private Sort getSortOption(String sort) {
        return switch (sort) {
            case "latest" -> Sort.by(Sort.Direction.DESC, "createdAt");
            case "popular" -> Sort.by(Sort.Direction.DESC, "likes");
            default -> throw new CustomException(PostErrorCode.INVALID_SORT_TYPE);
        };
    }

    @Transactional
    public void deletePost(String type, Long postId, Long memberId) {
        Post post = readPost(type, postId);

        if (!post.getMember().getId().equals(memberId)) {
            throw new CustomException(PostErrorCode.MEMBER_NOT_MATCH);
        }

        switch (type) {
            case "free" -> freePostRepository.delete((FreePost) post);
            case "piece" -> {
                PiecePost piecePost = (PiecePost) post;
                pieceRepository.delete(piecePost.getPiece());
                piecePostRepository.delete(piecePost);
            }
            default -> throw new CustomException(PostErrorCode.INVALID_POST_TYPE);
        }
    }

    private List<String> uploadImages(List<MultipartFile> images) {
        return images.stream()
                .map(image -> {
                    try {
                        return uploadUtil.upload(image, UploadUtil.BucketType.MEDIA, "post");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());
    }

    private FreePost createFreePost(Member member, PostRequestDto requestDto, List<String> imageUrls) {
        FreePost post = FreePost.builder()
                .title(requestDto.title())
                .content(requestDto.content())
                .imageUrls(imageUrls)
                .member(member)
                .anonymous(requestDto.anonymous())
                .build();
        return freePostRepository.save(post);
    }

    private PiecePost createPiecePost(Member member, PostRequestDto requestDto, List<String> imageUrls) {
        Venue venue = venueRepository.findById(requestDto.venueId())
                .orElseThrow(() -> new CustomException(PostErrorCode.VENUE_NOT_EXIST));
        PiecePostRequestDto request = (PiecePostRequestDto) requestDto;
        Piece piece = Piece.builder()
                .member(member)
                .venue(venue)
                .eventDate(request.eventDate())
                .totalPrice(request.totalPrice())
                .totalMembers(request.totalMembers())
                .build();
        pieceRepository.save(piece);

        PiecePost post = PiecePost.builder()
                .title(requestDto.title())
                .content(requestDto.content())
                .imageUrls(imageUrls)
                .member(member)
                .anonymous(requestDto.anonymous())
                .piece(piece)
                .build();
        return piecePostRepository.save(post);
    }

    public Post findPostByIdWithDiscriminator(Long postId) {
        // 먼저 자유 게시글 확인
        Optional<FreePost> freePost = freePostRepository.findById(postId);
        if (freePost.isPresent()) {
            return freePost.get();
        }

        // 없다면 조각모집 게시글 확인
        Optional<PiecePost> piecePost = piecePostRepository.findById(postId);
        if (piecePost.isPresent()) {
            return piecePost.get();
        }

        throw new CustomException(PostErrorCode.POST_NOT_EXIST);
    }


    @Transactional
    public void likePost(Long postId, Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(PostErrorCode.MEMBER_NOT_EXIST));


        Post post = findPostByIdWithDiscriminator(postId);

        PostInteractionId likeId = new PostInteractionId(memberId, post.getId());
        if (postLikeRepository.existsById(likeId)) {
            throw new CustomException(PostErrorCode.ALREADY_LIKED);
        }

        PostLike postLike = PostLike.builder()
                .post(post)
                .member(member)
                .id(likeId)
                .build();

        postLikeRepository.save(postLike);
        post.increaseLike();
    }


    @Transactional
    public void deletePostLike(Long postId, Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(PostErrorCode.MEMBER_NOT_EXIST));

        Post post = findPostByIdWithDiscriminator(postId);

        PostInteractionId likeId = new PostInteractionId(member.getId(), post.getId());
        PostLike postLike = postLikeRepository.findById(likeId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_LIKE));

        postLikeRepository.delete(postLike);
        post.decreaseLike();
    }

    @Transactional
    public void scrapPost(Long postId, Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(PostErrorCode.MEMBER_NOT_EXIST));

        Post post = findPostByIdWithDiscriminator(postId);

        PostInteractionId scrapId = new PostInteractionId(memberId, post.getId());
        if (postScrapRepository.existsById(scrapId)) {
            throw new CustomException(PostErrorCode.ALREADY_SCRAPPED);
        }

        PostScrap postScrap = PostScrap.builder()
                .post(post)
                .member(member)
                .id(scrapId)
                .build();

        postScrapRepository.save(postScrap);
    }


    @Transactional
    public void deletePostScrap(Long postId, Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(PostErrorCode.MEMBER_NOT_EXIST));

        Post post = findPostByIdWithDiscriminator(postId);

        PostInteractionId scrapId = new PostInteractionId(memberId, post.getId());
        PostScrap postScrap = postScrapRepository.findById(scrapId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_SCRAP));

        postScrapRepository.delete(postScrap);
    }

    /**
     * 지정한 회원이 스크랩한 게시글 중에서 주어진 타입("free" 또는 "piece")에 해당하는 게시글 목록을 페이지 단위로 조회합니다.
     *
     * @param memberId 조회할 회원의 ID
     * @param type     게시글 타입("free" 또는 "piece")
     * @param page     조회할 페이지 번호(0부터 시작)
     * @param size     페이지당 게시글 수
     * @return         필터링된 게시글 목록과 페이지 정보를 담은 PostListResponseDTO
     * @throws CustomException 회원이 존재하지 않거나 게시글 타입이 유효하지 않은 경우 발생
     */
    @Transactional(readOnly = true)
    public PostListResponseDTO getScrappedPostsByType(Long memberId, String type, int page, int size) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(PostErrorCode.MEMBER_NOT_EXIST));

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Post> postPage = postScrapRepository.findPostsByMemberId(memberId, pageable);

        List<PostPageResponseDTO> dtos = postPage.stream()
                .filter(post -> {
                    if (type.equals("free")) return post instanceof FreePost;
                    else if (type.equals("piece")) return post instanceof PiecePost;
                    else throw new CustomException(PostErrorCode.INVALID_POST_TYPE);
                })
                .map(PostPageResponseDTO::toDTO)
                .toList();

        return PostListResponseDTO.builder()
                .totalPost(dtos.size())
                .page(page)
                .size(size)
                .responseDTOS(dtos)
                .build();
    }


    /**
     * 지정한 회원이 작성한 게시글을 타입별로 페이징하여 조회합니다.
     *
     * @param memberId 조회할 회원의 ID
     * @param type 게시글 타입 ("free" 또는 "piece")
     * @param page 조회할 페이지 번호
     * @param size 페이지당 게시글 수
     * @return 페이징된 게시글 목록과 메타데이터를 포함한 응답 DTO
     * @throws CustomException 회원이 존재하지 않거나 게시글 타입이 유효하지 않은 경우 발생
     */
    @Transactional(readOnly = true)
    public PostListResponseDTO getMyPostsByType(Long memberId, String type, int page, int size) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(PostErrorCode.MEMBER_NOT_EXIST));

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Post> postPage;

        switch (type) {
            case "free" -> postPage = freePostRepository.findByMemberId(memberId, pageable);
            case "piece" -> postPage = piecePostRepository.findByMemberId(memberId, pageable);
            default -> throw new CustomException(PostErrorCode.INVALID_POST_TYPE);
        }

        List<PostPageResponseDTO> dtos = postPage
                .map(PostPageResponseDTO::toDTO)
                .toList();

        return PostListResponseDTO.builder()
                .totalPost((int) postPage.getTotalElements())
                .page(postPage.getNumber())
                .size(postPage.getSize())
                .responseDTOS(dtos)
                .build();
    }
}