package com.ceos.beatbuddy.domain.post.application;

import com.ceos.beatbuddy.domain.post.exception.PostErrorCode;
import com.ceos.beatbuddy.global.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class PostTypeHandlerFactory {
    private final Map<String, PostTypeHandler> handlerMap;

    public PostTypeHandler getHandler(String type) {
        PostTypeHandler handler = handlerMap.get(type);
        if (handler == null) {
            throw new CustomException(PostErrorCode.INVALID_POST_TYPE);
        }
        return handler;
    }
}
