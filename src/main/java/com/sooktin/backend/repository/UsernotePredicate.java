package com.sooktin.backend.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.sooktin.backend.domain.QUsernote;

public class UsernotePredicate {
    private static final QUsernote usernote = QUsernote.usernote;

    public static BooleanExpression containsKeyword(String keyword) {
        if (keyword==null||keyword.isEmpty()) return null;
        return usernote.content.containsIgnoreCase(keyword);
    }


}
