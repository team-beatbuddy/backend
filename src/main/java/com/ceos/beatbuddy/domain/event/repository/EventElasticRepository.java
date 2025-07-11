package com.ceos.beatbuddy.domain.event.repository;

import com.ceos.beatbuddy.domain.event.entity.EventDocument;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface EventElasticRepository extends ElasticsearchRepository<EventDocument, Long> {
     /* @param keyword 검색 키워드
     * @param isAdmin 관리자 여부
     * @return 검색된 EventDocument 리스트
     */
}
