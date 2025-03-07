package com.sooktin.backend.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sooktin.backend.domain.QUsernote;
import com.sooktin.backend.domain.Usernote;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.data.web.querydsl.QuerydslPredicateArgumentResolverSupport;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class UsernoteRepositoryCustomImpl implements UsernoteRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Usernote> searchUsernotesWithOrCondition(String keyword, Pageable pageable) {
        QUsernote usernote = QUsernote.usernote;

        BooleanBuilder builder = new BooleanBuilder();
        if (keyword != null && !keyword.trim().isEmpty()) {
            builder.or(usernote.content.containsIgnoreCase(keyword));
        }


        List<Usernote> content = queryFactory
                .selectFrom(usernote)
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // ✅ 최적화된 `fetchCount()`
        Long total = queryFactory
                .select(usernote.count())
                .from(usernote)
                .where(builder)
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }
}
