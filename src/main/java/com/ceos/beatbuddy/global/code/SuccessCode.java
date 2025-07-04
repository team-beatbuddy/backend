package com.ceos.beatbuddy.global.code;

import com.ceos.beatbuddy.global.ApiCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
public enum SuccessCode implements ApiCode {
    /**
     * User
     */
    SUCCESS_REISSUE(HttpStatus.OK, "토큰 재발급을 성공했습니다. 헤더 토큰을 확인하세요."),
    SUCCESS_REGISTER(HttpStatus.OK, "회원가입을 성공했습니다."),
    SUCCESS_LOGIN(HttpStatus.OK, "로그인을 성공했습니다. 헤더 토큰을 확인하세요."),
    SUCCESS_LOGOUT(HttpStatus.OK, "성공적으로 로그아웃했습니다."),

    /**
     * Member
     * */
    SUCCESS_BUSINESS_VERIFY_CODE(HttpStatus.OK, "성공적으로 인증 코드가 생성되었습니다."),
    SUCCESS_BUSINESS_VERIFY(HttpStatus.OK, "성공적으로 인증되었습니다"),
    SUCCESS_BUSINESS_SETTINGS(HttpStatus.OK, "성공적으로 프로필 세팅을 완료했습니다."),
    SUCCESS_UPLOAD_PROFILE_IMAGE(HttpStatus.OK, "성공적으로 프로필 사진을 추가했습니다."),
    SUCCESS_GET_PROFILE_SUMMARY(HttpStatus.OK, "프로필 요약 조회를 성공했습니다."),
    SUCCESS_UPDATE_NICKNAME(HttpStatus.OK, "닉네임을 성공적으로 변경했습니다."),

    /**
     * Admin
     * */
    SUCCESS_BUSINESS_APPROVED(HttpStatus.OK, "성공적으로 비즈니스 멤버를 승인하였습니다."),
    SUCCESS_BUSINESS_APPROVAL_LIST_RETRIEVED(HttpStatus.OK, "성공적으로 승인받을 비즈니스 멤버 리스트를 불러왔습니다."),

    /**
     * home
     * */
    SUCCESS_GET_MY_KEYWORD(HttpStatus.OK, "내가 선택한 키워드를 조회했습니다."),
    SUCCESS_GET_RECOMMEND_WITH_FAVORITE(HttpStatus.OK, "나의 취향에 맞는 베뉴 5개를 불러왔습니다."),
    SUCCESS_GET_RECOMMEND_WITH_FAVORITE_AND_FILTER(HttpStatus.OK, "선호도와 필터 조건에 맞는 베뉴 5개를 불러왔습니다."),


    /**
     * Magazine
     * */
    SUCCESS_CREATED_MAGAZINE(HttpStatus.CREATED, "매거진이 성공적으로 작성되었습니다."),
    SUCCESS_GET_MAGAZINE_LIST(HttpStatus.OK, "매거진을 성공적으로 불러왔습니다."),
    SUCCESS_SCRAP_MAGAZINE(HttpStatus.CREATED, "매거진이 성공적으로 스크랩되었습니다."),
    SUCCESS_DELETE_SCRAP(HttpStatus.OK, "스크랩을 취소했습니다."),
    SUCCESS_LIKE_MAGAZINE(HttpStatus.CREATED, "매거진에 성공적으로 좋아요를 표시했습니다."),
    SUCCESS_DELETE_LIKE(HttpStatus.OK, "좋아요를 취소했습니다."),

    /**
     * Event
     * */
    SUCCESS_CREATED_EVENT(HttpStatus.CREATED, "이벤트가 성공적으로 작성되었습니다."),
    SUCCESS_CREATED_EVENT_ATTENDANCE(HttpStatus.CREATED, "이벤트 참여 폼이 성공적으로 작성되었습니다."),
    SUCCESS_GET_UPCOMING_EVENT(HttpStatus.OK, "이벤트가 성공적으로 조회되었습니다."),
    SUCCESS_GET_EVENT_ATTENDANCE_LIST(HttpStatus.OK, "이벤트 참여자 명단을 조회했습니다."),
    SUCCESS_SCRAP_EVENT(HttpStatus.CREATED, "이벤트가 성공적으로 스크랩되었습니다."),
    SUCCESS_LIKE_EVENT(HttpStatus.CREATED, "이벤트에 성공적으로 좋아요 표시하었습니다."),
    SUCCESS_GET_EVENT(HttpStatus.OK, "성공적으로 이벤트를 조회했습니다." ),
    SUCCESS_GET_EVENT_COMMENTS(HttpStatus.OK, "성공적으로 댓글을 조회했습니다."),
    SUCCESS_GET_PAST_EVENT(HttpStatus.OK, "성공적으로 과거 이벤트를 조회했습니다." ),
    SUCCESS_GET_MY_PAGE_EVENTS(HttpStatus.OK, "마이페이지의 이벤트를 성공적으로 조회했습니다"),
    SUCCESS_GET_NOW_EVENT(HttpStatus.OK, "성공적으로 진행되는 이벤트를 조회했습니다."),
    SUCCESS_GET_MY_EVENTS(HttpStatus.OK, "성공적으로 내가 작성한 이벤트를 가져왔습니다."),
    SUCCESS_UPDATE_EVENT(HttpStatus.OK, "이벤트를 수정했습니다."),
    SUCCESS_DELETE_ATTENDANCE(HttpStatus.OK, "이벤트 참석을 취소했습니다."),
    SUCCESS_UPDATE_ATTENDANCE(HttpStatus.OK, "이벤트 참석 정보를 수정했습니다."),
    SUCCESS_GET_ATTENDANCE(HttpStatus.OK, "이벤트 참석 정보를 조회했습니다."),


    /**
     * comment
     * */
    SUCCESS_CREATED_COMMENT(HttpStatus.CREATED, "성공적으로 댓글을 작성했습니다."),
    SUCCESS_DELETE_COMMENT(HttpStatus.OK, "성공적으로 댓글을 삭제했습니다."),
    SUCCESS_UPDATE_COMMENT(HttpStatus.OK, "성공적으로 댓글을 수정했습니다."),


    /**
     * Follow
     * */
    SUCCESS_FOLLOW(HttpStatus.CREATED, "성공적으로 팔로우했습니다."),
    SUCCESS_FOLLOW_DELETE(HttpStatus.OK, "성공적으로 팔로우를 취소했습니다."),
    SUCCESS_GET_FOLLOWINGS(HttpStatus.OK, "내가 팔로우하는 목록을 가져왔습니다."),
    SUCCESS_GET_FOLLOWERS(HttpStatus.OK, "나를 팔로우하는 목록을 가져왔습니다."),


    /**
     * post
     * */
    SUCCESS_GET_POST_SORT_LIST(HttpStatus.OK, "type 에 맞는 post를 불러왔습니다."),
    SUCCESS_LIKE_POST(HttpStatus.CREATED, "좋아요를 눌렀습니다."),
    SUCCESS_SCRAP_POST(HttpStatus.CREATED, "스크랩을 완료했습니다."),
    GET_SCRAPPED_POST_LIST(HttpStatus.OK, "스크랩한 글을 불러왔습니다."),
    GET_MY_POST_LIST(HttpStatus.OK,"내가 작성한 글을 불러왔습니다."),
    SUCCESS_CREATE_POST(HttpStatus.CREATED, "포스트를 작성했습니다."),
    SUCCESS_GET_HOT_POSTS(HttpStatus.OK, "인기 게시글 상위 2개 조회했습니다."),
    SUCCESS_GET_POST(HttpStatus.OK, "포스트를 불러왔습니다"),
    SUCCESS_UPDATE_POST(HttpStatus.OK, "포스트를 수정했습니다."),
    SUCCESS_POST_SEARCH(HttpStatus.OK, "포스트 검색을 성공적으로 했습니다."),
    SUCCESS_GET_POST_LIST_BY_HASHTAG(HttpStatus.OK, "해시태그에 해당하는 포스트 목록을 성공적으로 조회했습니다."),

    /**
     * Venue
     * */
    SUCCESS_CREATE_VENUE_REVIEW(HttpStatus.CREATED, "베뉴 리뷰를 작성했습니다."),
    SUCCESS_GET_VENUE_REVIEW(HttpStatus.OK, "베뉴 리뷰를 조회했습니다."),
    SUCCESS_LIKE_VENUE_REVIEW(HttpStatus.CREATED, "베뉴 리뷰에 좋아요를 눌렀습니다."),
    SUCCESS_DELETE_VENUE_REVIEW(HttpStatus.OK, "베뉴 리뷰를 삭제했습니다."),
    SUCCESS_DELETE_VENUE_REVIEW_LIKE(HttpStatus.OK, "베뉴 리뷰 좋아요를 취소했습니다."),
    SUCCESS_UPDATE_VENUE_REVIEW(HttpStatus.OK, "베뉴 리뷰를 수정했습니다."),
    SUCCESS_VENUE_SEARCH(HttpStatus.OK, "베뉴 검색을 성공적으로 했습니다."),
    /**
     * empty
     * */
    SUCCESS_BUT_EMPTY_LIST(HttpStatus.OK,"성공적으로 조회했으나 리스트가 비었습니다."),
    ;
    private final HttpStatus httpStatus;
    private final String message;

    @Override
    public HttpStatus getStatus() {
        return this.httpStatus;
    }

    @Override
    public String getMessage() {
        return this.message;
    }
}