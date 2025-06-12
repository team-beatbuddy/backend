package com.ceos.beatbuddy.global;

import com.ceos.beatbuddy.domain.archive.exception.ArchiveErrorCode;
import com.ceos.beatbuddy.domain.comment.exception.CommentErrorCode;
import com.ceos.beatbuddy.domain.heartbeat.exception.HeartbeatErrorCode;
import com.ceos.beatbuddy.domain.magazine.exception.MagazineErrorCode;
import com.ceos.beatbuddy.domain.member.exception.MemberErrorCode;
import com.ceos.beatbuddy.domain.member.exception.MemberGenreErrorCode;
import com.ceos.beatbuddy.domain.member.exception.MemberMoodErrorCode;
import com.ceos.beatbuddy.domain.post.exception.PostErrorCode;
import com.ceos.beatbuddy.domain.search.exception.SearchErrorCode;
import com.ceos.beatbuddy.domain.vector.exception.VectorErrorCode;
import com.ceos.beatbuddy.domain.venue.exception.VenueErrorCode;
import com.ceos.beatbuddy.domain.venue.exception.VenueGenreErrorCode;
import com.ceos.beatbuddy.domain.venue.exception.VenueMoodErrorCode;
import com.ceos.beatbuddy.global.code.ErrorCode;
import com.ceos.beatbuddy.global.config.oauth.exception.OauthErrorCode;

public class CustomException extends ResponseException {

    public CustomException(MemberErrorCode memberErrorCode) {
        super(memberErrorCode);
    }

    public CustomException(MemberMoodErrorCode memberMoodErrorCode) {
        super(memberMoodErrorCode);
    }

    public CustomException(MemberGenreErrorCode memberMoodErrorCode) {
        super(memberMoodErrorCode);
    }

    public CustomException(VenueErrorCode venueErrorCode) {
        super(venueErrorCode);
    }

    public CustomException(VenueGenreErrorCode venueGenreErrorCode) {
        super(venueGenreErrorCode);
    }

    public CustomException(VenueMoodErrorCode venueMoodErrorCode) {
        super(venueMoodErrorCode);
    }

    public CustomException(HeartbeatErrorCode heartbeatErrorCode) {
        super(heartbeatErrorCode);
    }

    public CustomException(ArchiveErrorCode archiveErrorCode) {
        super(archiveErrorCode);
    }

    public CustomException(VectorErrorCode vectorErrorCode){
        super(vectorErrorCode);
    }

    public CustomException(SearchErrorCode searchErrorCode){
        super(searchErrorCode);
    }

    public CustomException(OauthErrorCode oauthErrorCode) {
        super(oauthErrorCode);
    }

    public CustomException(PostErrorCode postErrorCode) {
        super(postErrorCode);
    }

    public CustomException(CommentErrorCode commentErrorCode){
        super(commentErrorCode);
    }
    public CustomException(MagazineErrorCode magazineErrorCode){
        super(magazineErrorCode);
    }


    public CustomException(ApiCode apiCode) {
        super(apiCode);
    }
}
