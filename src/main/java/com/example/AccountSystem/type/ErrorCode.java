package com.example.AccountSystem.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    USER_NOT_FOUND("사용자를 찾을 수 없습니다."),
    MAX_ACCOUNT_PER_USER_10("계좌는 사용자당 최대 10개까지 만들 수 있습니다.")
    ;

    private final String description;
}
