package com.sooktin.backend.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sooktin.backend.domain.CareerCard;
import com.sooktin.backend.domain.QCareerCard;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class CareerCardRepositoryCustomImpl implements CareerCardRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<CareerCard> searchCareerCards(String keyword, Pageable pageable) {
        QCareerCard cc = QCareerCard.careerCard;

        BooleanBuilder builder = new BooleanBuilder();

        if (keyword != null && !keyword.isEmpty()) {
            String pattern = "%" + keyword + "%";
            builder.andAnyOf(
                    cc.major.like(pattern),
                    cc.department.like(pattern),
                    cc.job.like(pattern),
                    cc.experiences.any().company.like(pattern),
                    cc.skills.any().like(pattern)
            );
        }
        List<CareerCard> content = queryFactory
                .selectFrom(cc)
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(cc.count())
                .from(cc)
                .where(builder)
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    @Override
    public Page<CareerCard> searchCareerCardsWithOrCondition(String keyword, Pageable pageable) {
        QCareerCard cc = QCareerCard.careerCard;
        BooleanBuilder builder = new BooleanBuilder();

        if (keyword != null && !keyword.trim().isEmpty()) {
            String[] keywords = keyword.trim().split("\\s+");
            BooleanBuilder orBuilder = new BooleanBuilder();

            for (String singleKeyword : keywords) {
                BooleanBuilder condition = new BooleanBuilder();
                boolean isExactMatch = singleKeyword.startsWith("\"") && singleKeyword.endsWith("\"");
                singleKeyword = singleKeyword.replaceAll("^\"|\"$", "");

                if (isExactMatch) {
                    condition.or(cc.major.equalsIgnoreCase(singleKeyword))
                            .or(cc.department.equalsIgnoreCase(singleKeyword))
                            .or(cc.job.equalsIgnoreCase(singleKeyword))
                            .or(cc.experiences.any().company.equalsIgnoreCase(singleKeyword))
                            .or(cc.skills.any().equalsIgnoreCase(singleKeyword));
                } else {
                    String pattern = "%" + singleKeyword + "%";
                    condition.or(cc.major.likeIgnoreCase(pattern))
                            .or(cc.department.likeIgnoreCase(pattern))
                            .or(cc.job.likeIgnoreCase(pattern))
                            .or(cc.experiences.any().company.likeIgnoreCase(pattern))
                            .or(cc.skills.any().likeIgnoreCase(pattern));
                }

                orBuilder.or(condition);
            }

            builder.and(orBuilder);
        }

        List<CareerCard> content = queryFactory
                .selectFrom(cc)
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(cc.count())
                .from(cc)
                .where(builder)
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }
}
