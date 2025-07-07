package com.ceos.beatbuddy.global;

public class SwaggerExamples {
    /**
     * 200
     * */
    public static final String SUCCESS_BUT_EMPTY_LIST = """
    {
      "status": 200,
      "code": "SUCCESS_BUT_EMPTY_LIST",
      "message": "성공적으로 조회했으나 리스트가 비었습니다.",
      "data": []
    }
    """;
    
    /**
     * 400 잘못된 요청
     * */
    public static final String INVALID_POST_TYPE = """
        {
          "status": 400,
          "error": "BAD_REQUEST",
          "code": "INVALID_POST_TYPE",
          "message": "포스트의 type이 올바르지 않습니다"
        }
        """;
    
    public static final String NEED_DEPOSIT_INFO = """
        {
          "status": 400,
          "error": "BAD_REQUEST",
          "code": "NEED_DEPOSIT_INFO",
          "message": "예약금에 관련된 정보가 필요합니다."
        }
        """;

    public static final String INVALID_RECEIVE_INFO_CONFIGURATION = """
        {
          "status": 400,
          "error": "BAD_REQUEST",
          "code": "INVALID_RECEIVE_INFO_CONFIGURATION",
          "message": "receiveInfo가 false일 때 다른 수집 항목은 true일 수 없습니다."
        }
        """;

    public static final String MISSING_NAME_EXAMPLE = """
        {
          "status": 400,
          "error": "BAD_REQUEST",
          "code": "MISSING_NAME",
          "message": "이름 입력은 필수입니다."
        }
        """;

    public static final String MISSING_GENDER_EXAMPLE = """
        {
          "status": 400,
          "error": "BAD_REQUEST",
          "code": "MISSING_GENDER",
          "message": "성별 입력은 필수입니다."
        }
        """;

    public static final String MISSING_PHONE_EXAMPLE = """
        {
          "status": 400,
          "error": "BAD_REQUEST",
          "code": "MISSING_PHONE",
          "message": "핸드폰 번호 입력은 필수입니다."
        }
        """;

    public static final String MISSING_TOTAL_COUNT_EXAMPLE = """
        {
          "status": 400,
          "error": "BAD_REQUEST",
          "code": "MISSING_TOTAL_COUNT",
          "message": "동행인원 입력은 필수입니다."
        }
        """;

    public static final String MISSING_SNS_INFO_EXAMPLE = """
        {
          "status": 400,
          "error": "BAD_REQUEST",
          "code": "MISSING_SNS_ID_OR_TYPE",
          "message": "SNS ID 또는 TYPE 입력은 필수입니다."
        }
        """;

    public static final String MISSING_PAYMENT_EXAMPLE = """
        {
          "status": 400,
          "error": "BAD_REQUEST",
          "code": "MISSING_PAYMENT",
          "message": "지불 완료 입력은 필수입니다."
        }
        """;

    public static final String INVALID_GENDER_EXAMPLE = """
        {
          "status": 400,
          "error": "BAD_REQUEST",
          "code": "INVALID_GENDER",
          "message": "성별 값이 올바르지 않습니다. (MALE, FEMALE 중 하나여야 합니다.)"
        }
        """;

    public static final String TOO_MANY_IMAGES_5_EXAMPLE = """
        {
          "status": 400,
          "error": "BAD_REQUEST",
          "code": "TOO_MANY_IMAGES_5",
          "message": "이미지는 최대 5개까지 업로드할 수 있습니다."
        }
        """;

    public static final String TOO_MANY_IMAGES_10_EXAMPLE = """
        {
          "status": 400,
          "error": "BAD_REQUEST",
          "code": "TOO_MANY_IMAGES_10",
          "message": "이미지는 최대 10개까지 업로드할 수 있습니다."
        }
        """;

    public static final String TOO_MANY_IMAGES_20_EXAMPLE = """
        {
          "status": 400,
          "error": "BAD_REQUEST",
          "code": "TOO_MANY_IMAGES_20",
          "message": "이미지는 최대 20개까지 업로드할 수 있습니다."
        }
        """;

    public static final String SAME_NICKNAME = """
        {
          "status": 400,
          "error": "BAD_REQUEST",
          "code": "SAME_NICKNAME",
          "message": "동일한 닉네임으로는 변경이 불가능합니다."
        }
        """;

    public static final String NICKNAME_CHANGE_LIMITED = """
        {
          "status": 400,
          "error": "BAD_REQUEST",
          "code": "NICKNAME_CHANGE_LIMITED",
          "message": "닉네임 변경은 14일 내에 2번까지만 가능합니다. 14일 뒤에 변경해주세요."
        }
        """;

    public static final String PAGE_OUT_OF_BOUNDS = """
        {
          "status": 400,
          "error": "BAD_REQUEST",
          "code": "PAGE_OUT_OF_BOUNDS",
          "message": "페이지 번호가 범위를 벗어났습니다."
        }
        """;

    // 두글자 이상 검색
    public static final String KEYWORD_TOO_SHORT = """
        {
            "status":400,
            "error":"BAD_REQUEST",
            "code":"BAD_REQUEST_VALIDATION",
            "message":"요청 값이 유효하지 않습니다."
            "errors": {
                "keyword": "2글자 이상 입력해야 합니다."
            }
        }
        """;

    public static final String EMPTY_KEYWORD = """
        {
            "status":400,
            "error":"BAD_REQUEST",
            "code":"BAD_REQUEST_VALIDATION",
            "message":"요청 값이 유효하지 않습니다."
            "errors": {
                "keyword": "검색 시, 키워드는 필수입니다."
            }
        }
        """;

    public static final String DUPLICATED_HASHTAG = """
        {
          "status": 400,
          "error": "BAD_REQUEST",
          "code": "DUPLICATED_HASHTAG",
          "message": "해시태그는 중복될 수 없습니다."
        }
        """;

    // 홈 고정 매거진
    public static final String INVALID_ORDER_IN_HOME = """
        {
          "status": 400,
          "error": "BAD_REQUEST",
          "code": "INVALID_ORDER_IN_HOME",
          "message": "홈에서의 순서가 잘못되었습니다. 고정된 매거진은 1 이상의 순서를 가져야 합니다."
        }
        """;

    public static final String DUPLICATE_ORDER_IN_HOME = """
        {
          "status": 400,
          "error": "BAD_REQUEST",
          "code": "DUPLICATE_ORDER_IN_HOME",
          "message": "홈에서의 순서가 중복되었습니다. 이미 존재하는 순서를 사용하고 있습니다."
        }
        """;

    public static final String COUPON_NOT_FOUND = """
        {
          "status": 400,
          "error": "BAD_REQUEST",
          "code": "COUPON_NOT_FOUND",
          "message": "존재하지 않는 쿠폰입니다."
        }
        """;

    public static final String COUPON_QUOTA_SOLD_OUT = """
        {
          "status": 400,
          "error": "BAD_REQUEST",
          "code": "COUPON_QUOTA_SOLD_OUT",
          "message": "쿠폰의 수량이 모두 소진되었습니다."
        }
        """;

    public static final String COUPON_QUOTA_NOT_INITIALIZED = """
        {
          "status": 400,
          "error": "BAD_REQUEST",
          "code": "COUPON_QUOTA_NOT_INITIALIZED",
          "message": "쿠폰의 수량이 초기화되지 않았습니다."
        }
        """;

    public static final String COUPON_EXPIRED = """
        {
          "status": 400,
          "error": "BAD_REQUEST",
          "code": "COUPON_EXPIRED",
          "message": "쿠폰이 만료되었습니다."
        }
        """;

    public static final String COUPON_DISABLED = """
        {
          "status": 400,
          "error": "BAD_REQUEST",
          "code": "COUPON_DISABLED",
          "message": "활성화되지 않은 쿠폰입니다."
        }
        """;

    public static final String COUPON_INVALID_POLICY = """
        {
          "status": 400,
          "error": "BAD_REQUEST",
          "code": "COUPON_INVALID_POLICY",
          "message": "유효하지 않은 쿠폰 정책입니다."
        }
        """;

    public static final String COUPON_ALREADY_USED = """
        {
          "status": 400,
          "error": "BAD_REQUEST",
          "code": "COUPON_ALREADY_USED",
          "message": "이미 사용된 쿠폰입니다."
        }
        """;

    public static final String COUPON_RECEIVE_LIMIT_EXCEEDED = """
        {
          "status": 400,
          "error": "BAD_REQUEST",
          "code": "COUPON_RECEIVE_LIMIT_EXCEEDED",
          "message": "쿠폰 수령 한도를 초과했습니다."
        }
        """;

    /**
     * 403 권한 없음
     * */
    public static final String UNAUTHORIZED_MEMBER = """
        {
          "status": 403,
          "error": "FORBIDDEN",
          "code": "UNAUTHORIZED_MEMBER",
          "message": "해당 작업에 대한 권한이 없습니다."
        }
        """;

    public static final String FORBIDDEN_EVENT_ACCESS = """
        {
          "status": 403,
          "error": "FORBIDDEN",
          "code": "FORBIDDEN_EVENT_ACCESS",
          "message": "해당 이벤트에 대한 접근 권한이 없습니다."
        }
        """;

    public static final String CANNOT_ADD_EVENT_UNAUTHORIZED_MEMBER = """
    {
      "status": 403,
      "error": "FORBIDDEN",
      "code": "CANNOT_ADD_EVENT_UNAUTHORIZED_MEMBER",
      "message": "글을 작성할 수 없는 유저입니다."
    }
    """;

    public static final String CANNOT_ADD_MAGAZINE_UNAUTHORIZED_MEMBER = """
        {
          "status": 403,
          "error": "FORBIDDEN",
          "code": "CANNOT_ADD_MAGAZINE_UNAUTHORIZED_MEMBER",
          "message": "매거진을 작성할 수 없는 유저입니다."
        }
        """;



    
    /**
     * 404 리소스 없음
     * */

    public static final String MEMBER_NOT_EXIST = """
        {
          "status": 404,
          "error": "NOT_FOUND",
          "code": "MEMBER_NOT_EXIST",
          "message": "요청한 유저가 존재하지 않습니다."
        }
        """;

    public static final String VENUE_NOT_EXIST = """
        {
          "status": 404,
          "error": "NOT_FOUND",
          "code": "VENUE_NOT_EXIST",
          "message": "존재하지 않는 베뉴입니다."
        }
        """;

    public static final String POST_NOT_EXIST = """
        {
          "status": 404,
          "error": "NOT_FOUND",
          "code": "POST_NOT_EXIST",
          "message": "존재하지 않는 포스트입니다."
        }
        """;

    public static final String NOT_FOUND_EVENT = """
        {
          "status": 404,
          "error": "NOT_FOUND",
          "code": "NOT_FOUND_EVENT",
          "message": "존재하지 않는 이벤트입니다."
        }
        """;
    
    public static final String NOT_FOUND_SCRAP = """
        {
          "status": 404,
          "error": "NOT_FOUND",
          "code": "NOT_FOUND_SCRAP",
          "message": "기존에 스크랩하지 않았습니다. 스크랩을 취소할 수 없습니다."
        }
        """;

    public static final String NOT_FOUND_LIKE = """
        {
          "status": 404,
          "error": "NOT_FOUND",
          "code": "NOT_FOUND_LIKE",
          "message": "기존에 좋아요를 누르지 않았습니다. 좋아요를 취소할 수 없습니다."
        }
        """;

    public static final String NOT_FOUND_COMMENT = """
        {
          "status": 404,
          "error": "NOT_FOUND",
          "code": "NOT_FOUND_COMMENT",
          "message": "해당 댓글을 찾을 수 없습니다."
        }
        """;

    public static final String ATTENDANCE_NOT_FOUND = """
        {
          "status": 404,
          "error": "NOT_FOUND",
          "code": "ATTENDANCE_NOT_FOUND",
          "message": "해당 이벤트에 대한 참석 정보를 찾을 수 없습니다."
        }
        """;

    public static final String NOT_FOUND_COMMENT_IN_EVENT = """
        {
          "status": 404,
          "error": "NOT_FOUND",
          "code": "NOT_FOUND_COMMENT_IN_EVENT",
          "message": "해당 댓글이 이벤트에 속하지 않습니다."
        }
        """;

    public static final String NOT_FOUND_VENUE_REVIEW = """
        {
          "status": 404,
          "error": "NOT_FOUND",
          "code": "NOT_FOUND_VENUE_REVIEW",
          "message": "존재하지 않는 베뉴 리뷰입니다."
        }
        """;

    public static final String NOT_FOUND_MAGAZINE = """
        {
          "status": 404,
          "error": "NOT_FOUND",
          "code": "NOT_FOUND_MAGAZINE",
          "message": "존재하지 않는 매거진입니다."
        }
        """;

    public static final String NOT_FOUND_HASHTAG = """
        {
          "status": 404,
          "error": "NOT_FOUND",
          "code": "NOT_FOUND_HASHTAG",
          "message": "존재하지 않는 해시태그입니다."
        }
        """;

    public static final String NOT_FOUND_COUPON = """
        {
          "status": 404,
          "error": "NOT_FOUND",
          "code": "NOT_FOUND_COUPON",
          "message": "존재하지 않는 쿠폰입니다."
        }
        """;

    public static final String NOT_FOUND_COUPON_RECEIVE = """
        {
          "status": 404,
          "error": "NOT_FOUND",
          "code": "NOT_FOUND_COUPON_RECEIVE",
          "message": "존재하지 않는 쿠폰 수령 정보입니다."
        }
        """;


    /**
     * 409 already do something
     * */
    public static final String ALREADY_SCRAPPED = """
        {
          "status": 409,
          "error": "CONFLICT",
          "code": "ALREADY_SCRAPPED",
          "message": "이미 스크랩을 눌렀습니다."
        }
        """;

    public static final String ALREADY_LIKED = """
        {
          "status": 409,
          "error": "CONFLICT",
          "code": "ALREADY_LIKED",
          "message": "이미 좋아요를 눌렀습니다."
        }
        """;

    public static final String ALREADY_ATTENDANCE_EVENT = """
        {
          "status": 409,
          "error": "CONFLICT",
          "code": "ALREADY_ATTENDANCE_EVENT",
          "message": "이미 참여 신청한 이벤트입니다."
        }
        """;

    public static final String COUPON_ALREADY_RECEIVED = """
        {
          "status": 409,
          "error": "CONFLICT",
          "code": "COUPON_ALREADY_RECEIVED",
          "message": "이미 받은 쿠폰입니다."
        }
        """;

    public static final String COUPON_ALREADY_RECEIVED_TODAY = """
        {
          "status": 409,
          "error": "CONFLICT",
          "code": "COUPON_ALREADY_RECEIVED_TODAY",
          "message": "이미 오늘 해당 쿠폰을 수령하셨습니다."
        }
        """;


    /**
     * 500, Server or S3 Error
     * */
    public static final String IMAGE_UPLOAD_FAILED = """
        {
          "status": 500,
          "error": "INTERNAL_SERVER_ERROR",
          "code": "IMAGE_UPLOAD_FAILED",
          "message": "이미지 업로드에 실패했습니다."
        }
        """;

    public static final String IMAGE_DELETE_FAILED = """
        {
          "status": 500,
          "error": "INTERNAL_SERVER_ERROR",
          "code": "IMAGE_DELETE_FAILED",
          "message": "이미지 삭제에 실패했습니다."
        }
        """;

    public static final String ELASTICSEARCH_POST_CREATE_FAILED = """
        {
          "status": 500,
          "error": "INTERNAL_SERVER_ERROR",
          "code": "ELASTICSEARCH_POST_CREATE_FAILED",
          "message": "Elasticsearch 인덱싱에 실패했습니다."
        }
        """;

    public static final String ELASTICSEARCH_POST_DELETE_FAILED = """
        {
          "status": 500,
          "error": "INTERNAL_SERVER_ERROR",
          "code": "ELASTICSEARCH_POST_DELETE_FAILED",
          "message": "Elasticsearch 삭제에 실패했습니다."
        }
        """;

    public static final String ELASTICSEARCH_SEARCH_FAILED = """
        {
          "status": 500,
          "error": "INTERNAL_SERVER_ERROR",
          "code": "ELASTICSEARCH_SEARCH_FAILED",
          "message": "Elasticsearch 검색에 실패했습니다."
        }
        """;

    // 다른 공통 예시도 여기에 추가 가능
}