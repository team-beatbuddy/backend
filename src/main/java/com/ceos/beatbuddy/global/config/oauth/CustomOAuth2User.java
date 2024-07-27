package com.ceos.beatbuddy.global.config.oauth;

import com.ceos.beatbuddy.global.config.oauth.dto.Oauth2MemberDto;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

@RequiredArgsConstructor
public class CustomOAuth2User implements OAuth2User {

    private final Oauth2MemberDto memberDto;

    @Override
    public Map<String, Object> getAttributes() {
        return null;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> collection = new ArrayList<>();

        collection.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                return memberDto.getRole();
            }
        });

        return collection;
    }

    @Override
    public String getName() {
        return memberDto.getName();
    }

    public String getUsername() {

        return memberDto.getLoginId();
    }

    public Long getMemberId() {
        return memberDto.getMemberId();
    }

}
