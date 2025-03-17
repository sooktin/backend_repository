package com.sooktin.backend.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sooktin.backend.domain.QUsernote;
import com.sooktin.backend.domain.Usernote;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.data.web.querydsl.QuerydslPredicateArgumentResolverSupport;
import org.springframework.stereotype.Repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;

import java.util.List;
import java.util.Set;

@Slf4j
@Repository
@RequiredArgsConstructor
public class UsernoteRepositoryCustomImpl implements UsernoteRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Usernote> searchUsernotesWithOrCondition(String keyword, Pageable pageable) {
        Logger log = LoggerFactory.getLogger(this.getClass());
        log.info("🔍 검색 요청 - keyword: \"{}\"", keyword);

        QUsernote usernote = QUsernote.usernote;
        BooleanBuilder builder = new BooleanBuilder();

        if (keyword != null && !keyword.trim().isEmpty()) {
            keyword = keyword.trim().replaceAll("\\s+", " "); // 🔹 연속 공백 정리
            log.info("📌 공백 정리된 keyword: \"{}\"", keyword);

            String[] keywords = keyword.split(" "); // 🔹 키워드를 공백 기준으로 분리
            log.info("📌 분리된 키워드: \"{}\"", Arrays.toString(keywords));

            BooleanBuilder orBuilder = new BooleanBuilder();
            Set<String> allKeywords = new HashSet<>(Arrays.asList(keywords)); // 키워드 목록 저장

            for (String singleKeyword : keywords) {
                BooleanBuilder condition = new BooleanBuilder();
                boolean isExactMatch = singleKeyword.startsWith("\"") && singleKeyword.endsWith("\""); // 🔹 큰따옴표 포함 여부 확인
                singleKeyword = singleKeyword.replaceAll("^\"|\"$", ""); // 🔹 큰따옴표 제거
                String lowerKeyword = singleKeyword.toLowerCase(); // 🔹 검색어 소문자로 변환

                log.info("🔹 처리된 키워드 - isExactMatch: \"{}\", lowerKeyword: \"{}\"", isExactMatch, lowerKeyword);

                if (isExactMatch) {
                    // 🔹 정확한 단어 검색 (대소문자 무시, 부분 포함 X)
                    condition.or(usernote.content.lower().like("% " + lowerKeyword + " %")) // 단어 단위 검색
                            .or(usernote.content.lower().like(lowerKeyword + " %")) // 문장 앞
                            .or(usernote.content.lower().like("% " + lowerKeyword)) // 문장 끝
                            .or(usernote.content.lower().eq(lowerKeyword)); // 전체 일치
                    log.info("✅ 정확한 단어 검색 추가 - \"{}\"", lowerKeyword);
                } else {
                    // 🔹 LIKE 검색 (단어 포함 검색, "java"로 검색할 때 "JavaScript" 제외)
                    condition.or(usernote.content.lower().like("% " + lowerKeyword + " %")) // 단어 단위 검색
                            .or(usernote.content.lower().like(lowerKeyword + " %"))
                            .or(usernote.content.lower().like("% " + lowerKeyword))
                            .or(usernote.content.lower().like("%" + lowerKeyword + "%")); // 🔥 추가된 조건
                }

                orBuilder.or(condition);
            }

            // 🔹 "부분 포함되는 단어" 관계를 자동으로 탐지하여 제외 조건 추가
            for (String keyword1 : allKeywords) {
                for (String keyword2 : allKeywords) {
                    if (!keyword1.equals(keyword2) && keyword2.contains(keyword1)) {
                        // 🔹 keyword1이 keyword2의 일부일 경우, keyword2를 제외하는 조건 추가 (단어 경계 포함)
                        builder.and(usernote.content.lower().notLike("% " + keyword2 + " %"))
                                .and(usernote.content.lower().notLike(keyword2 + " %"))
                                .and(usernote.content.lower().notLike("% " + keyword2))
                                .and(usernote.content.lower().notLike("%" + keyword2 + "%")); // 🔥 최종 제외
                        log.info("❌ '{}'는 '{}'의 일부이므로 제외", keyword1, keyword2);
                    }
                }
            }

            builder.and(orBuilder); // 🔹 OR 조건 적용
        }

        log.info("🟢 Query 실행 전 - builder: {}", builder);

        List<Usernote> content = queryFactory
                .selectFrom(usernote)
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        log.info("🟢 Query 실행 후 - 결과 개수: {}", content.size());

        Long total = queryFactory
                .select(usernote.count())
                .from(usernote)
                .where(builder)
                .fetchOne();

        log.info("🟢 총 검색 결과 개수: {}", total);

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }
}
