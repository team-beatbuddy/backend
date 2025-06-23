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


    // 다른 공통 예시도 여기에 추가 가능
}